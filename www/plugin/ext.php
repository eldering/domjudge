<?php
/**
 * Output event feed in XML format according to Kattis specs.
 *
 * Part of the DOMjudge Programming Contest Jury System and licenced
 * under the GNU GPL. See README and COPYING for details.
 */

require('init.php');

require(LIBWWWDIR . '/scoreboard.php');

// needed for short verdicts
$result_map = array(
	'correct' => 'AC',
	'compiler-error' => 'CTE',
	'timelimit' => 'TLE',
	'run-error' => 'RTE',
	'no-output' => 'NO',
	'wrong-answer' => 'WA',
	'presentation-error' => 'PE',
	'memory-limit' => 'MLE',
	'output-limit' => 'OLE'
);

// Get problems, languages, affiliations, categories and events
$probs = $DB->q('KEYTABLE SELECT probid AS ARRAYKEY, name, color FROM problem
                 WHERE cid = %i AND allow_submit = 1 ORDER BY probid', $cid);

$langs = $DB->q('KEYTABLE SELECT langid AS ARRAYKEY, name FROM language
                 WHERE allow_submit = 1 ORDER BY langid');

$teams = $DB->q('KEYTABLE SELECT login AS ARRAYKEY, name, affilid, categoryid
                 FROM team ORDER BY login');

$affils = $DB->q('KEYTABLE SELECT affilid AS ARRAYKEY, name, country
                  FROM team_affiliation ORDER BY name');

$categs = $DB->q('KEYTABLE SELECT categoryid AS ARRAYKEY, name, color
                  FROM team_category WHERE visible = 1 ORDER BY name');

$events = $DB->q('SELECT * FROM event WHERE cid = %i AND ' .
                 (isset($_REQUEST['fromid']) ? 'eventid >= %i ' : 'TRUE %_ ') . 'AND ' .
                 (isset($_REQUEST['toid'])   ? 'eventid <  %i ' : 'TRUE %_ ') .
                 'ORDER BY eventid', $cid, (int)@$_REQUEST['fromid'], (int)@$_REQUEST['toid']);

$xmldoc = new DOMDocument('1.0', DJ_CHARACTER_SET);

$root       = XMLaddnode($xmldoc, 'contest');
$reset      = XMLaddnode($root, 'reset');
$info       = XMLaddnode($root, 'info');

// write out general info
$length = strtotime($cdata['endtime']) - strtotime($cdata['starttime']);
$lengthString = sprintf('%02d:%02d:%02d', $length/(60*60), ($length/60) % 60, $length % 60);
XMLaddnode($info, 'length', $lengthString);
XMLaddnode($info, 'penalty', 20);
XMLaddnode($info, 'started', 'False');
XMLaddnode($info, 'starttime', strtotime($cdata['starttime']));
XMLaddnode($info, 'title', $cdata['contestname']);

// write out problems
$id_cnt = 0;
foreach( $probs as $prob => $data ) {
	$id_cnt++;
	$prob_to_id[$prob] = $id_cnt;
	$node = XMLaddnode($root, 'problem');
	XMLaddnode($node, 'id', $id_cnt);
	XMLaddnode($node, 'name', $data['name']);
}

// write out languages
$id_cnt = 0;
foreach( $langs as $lang => $data ) {
	$id_cnt++;
	$lang_to_id[$lang] = $id_cnt;
	$node = XMLaddnode($root, 'language');
	XMLaddnode($node, 'id', $id_cnt);
	XMLaddnode($node, 'name', $data['name']);
}

// write out teams
$id_cnt = 0;
foreach( $teams as $team => $data ) {
	if (!isset($categs[$data['categoryid']])) continue;
	$id_cnt++;
	$team_to_id[$team] = $id_cnt;
	$node = XMLaddnode($root, 'team');
	XMLaddnode($node, 'id', $id_cnt);
	XMLaddnode($node, 'name', $data['name']);
	if ( isset($data['affilid']) ) {
		XMLaddnode($node, 'nationality', $affils[$data['affilid']]['country']);
		XMLaddnode($node, 'university', $data['affilid']);
	}
}

// write out runs
while ( $row = $events->next() ) {
	if ( $row['description'] != 'problem submitted' &&
	     $row['description'] != 'problem judged') {
		continue;
	}

	$data = $DB->q('MAYBETUPLE SELECT submittime, teamid, probid, langid, valid
	                FROM submission WHERE valid = 1 AND submitid = %i',
	               $row['submitid']);

	if ( empty($data) ||
	     difftime($data['submittime'], $cdata['endtime'])>=0 ||
	     !isset($prob_to_id[$data['probid']]) ||
	     !isset($lang_to_id[$data['langid']]) ||
	     !isset($team_to_id[$data['teamid']]) ) continue;

	$run = XMLaddnode($root, 'run');
	XMLaddnode($run, 'id', $row['submitid']);
	XMLaddnode($run, 'language', $lang_to_id[$data['langid']]);
	XMLaddnode($run, 'problem', $prob_to_id[$data['probid']]);
	XMLaddnode($run, 'team', $team_to_id[$data['teamid']]);
	XMLaddnode($run, 'timestamp', strtotime($row['eventtime']));
	XMLaddnode($run, 'time', strtotime($data['submittime']) - strtotime($cdata['starttime']));

	if ($row['description'] == 'problem submitted') {
		XMLaddnode($run, 'judged', 'False');
		XMLaddnode($run, 'status', 'fresh');
	} else {
		$result = $DB->q('MAYBEVALUE SELECT result FROM judging j
		                  LEFT JOIN submission USING(submitid)
		                  WHERE j.valid = 1 AND judgingid = %i', $row['judgingid']);

		if (!isset($result)) continue;

		XMLaddnode($run, 'judged', 'True');
		XMLaddnode($run, 'status', 'done');
		XMLaddnode($run, 'result', $result_map[$result]);
		if ( $result == 'correct' ) {
			XMLaddnode($run, 'solved', 'True');
			XMLaddnode($run, 'penalty', 'False');
		} else {
			XMLaddnode($run, 'solved', 'False');
			XMLaddnode($run, 'penalty', 'True');
		}
	}
}

header('Content-Type: text/xml; charset=' . DJ_CHARACTER_SET);

$xmldoc->formatOutput = false;
echo $xmldoc->saveXML();
