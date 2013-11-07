package de.fau.cs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashSet;

import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class ContestInfo {
	String length, penalty, started, starttime, title;

	ContestInfo(Element info) {
		length = XMLHelper.getTextValue(info, "length");
		penalty = XMLHelper.getTextValue(info, "penalty");
		started = XMLHelper.getTextValue(info, "started");
		starttime = XMLHelper.getTextValue(info, "starttime");
		title = XMLHelper.getTextValue(info, "title");
	}

	public String toString() {
		String ret = XMLHelper.s("info");
		ret += XMLHelper.simpleElement("length", length);
		ret += XMLHelper.simpleElement("penalty", penalty);
		ret += XMLHelper.simpleElement("started", started);
		ret += XMLHelper.simpleElement("starttime", starttime);
		ret += XMLHelper.simpleElement("title", title);
		ret += XMLHelper.e("info");
		return ret;
	}
}

class Run {

	@Override
	public int hashCode() {
		long r = Long.parseLong(timestamp);
		r *= 10000;
		r += Long.parseLong(id);
		return (int) (r % ((1 << 31) - 1));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Run other = (Run) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (judged == null) {
			if (other.judged != null)
				return false;
		} else if (!judged.equals(other.judged))
			return false;
		if (penalty == null) {
			if (other.penalty != null)
				return false;
		} else if (!penalty.equals(other.penalty))
			return false;
		if (problem == null) {
			if (other.problem != null)
				return false;
		} else if (!problem.equals(other.problem))
			return false;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result))
			return false;
		if (solved == null) {
			if (other.solved != null)
				return false;
		} else if (!solved.equals(other.solved))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (team == null) {
			if (other.team != null)
				return false;
		} else if (!team.equals(other.team))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}

	String id, problem, team, timestamp, time, judged, status, result, solved,
			penalty;

	Run(Element run) {
		id = XMLHelper.getTextValue(run, "id");
		problem = XMLHelper.getTextValue(run, "problem");
		team = XMLHelper.getTextValue(run, "team");
		timestamp = XMLHelper.getTextValue(run, "timestamp");
		time = XMLHelper.getTextValue(run, "time");
		judged = XMLHelper.getTextValue(run, "judged");
		status = XMLHelper.getTextValue(run, "status");
		result = XMLHelper.getTextValue(run, "result");
		solved = XMLHelper.getTextValue(run, "solved");
		penalty = XMLHelper.getTextValue(run, "penalty");
	}

	public String toString() {
		String ret = XMLHelper.s("run");
		ret += XMLHelper.simpleElement("id", id);
		ret += XMLHelper.simpleElement("team", team);
		ret += XMLHelper.simpleElement("problem", problem);
		ret += XMLHelper.simpleElement("timestamp", timestamp);
		ret += XMLHelper.simpleElement("time", time);
		ret += XMLHelper.simpleElement("judged", judged);
		ret += XMLHelper.simpleElement("status", status);
		if (result != null) {
			ret += XMLHelper.simpleElement("result", result);
		}
		if (solved != null) {
			ret += XMLHelper.simpleElement("solved", solved);
		}
		if (penalty != null) {
			ret += XMLHelper.simpleElement("penalty", penalty);
		}
		ret += XMLHelper.e("run");
		return ret;
	}
}

class XMLHelper {

	public static String getTextValue(Element element, String tagName) {
		String textVal = null;
		NodeList nodeList = element.getElementsByTagName(tagName);
		if ((nodeList != null) && (nodeList.getLength() > 0)) {
			Element child = (Element) nodeList.item(0);
			textVal = child.getFirstChild().getNodeValue();
		}

		return textVal;
	}

	public static String simpleElement(String tag, String content) {
		return s(tag) + StringEscapeUtils.escapeXml(content) + e(tag);
	}

	public static String s(String start) {
		return "<" + start + ">";
	}

	public static String e(String end) {
		return "</" + end + ">";
	}
}

public class DOMjudgeFeed implements ActionListener {

	private ContestInfo contestInfo = null;
	private HashSet<String> problems = new HashSet<String>();
	private HashSet<String> teams = new HashSet<String>();
	private HashSet<Run> runs = new HashSet<Run>();
	private String filename;

	DOMjudgeFeed(String filename) {
		this.filename = filename;

		System.out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		System.out.println("<contest>\n");
		System.out.println("<reset/>\n");
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Usage: ./feed URI [user pass], e.g. ./feed http://domjudge.cs.fau.de/plugin/ext.php");
			System.exit(-1);
		}
		DOMjudgeFeed feed = new DOMjudgeFeed(args[0]);

		if (args.length >= 3) {
			final String user = args[1];
			final String password = args[2];
			Authenticator.setDefault(new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(user, password
							.toCharArray());
				}
			});
		}

		Timer timer = new Timer(5000, feed);
		timer.start();
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
			}
		}
	}

	public Document parseXmlFile() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(filename);
			return dom;
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return null;
	}

	public void parseDocument(Document dom) {
		Element contest = dom.getDocumentElement();

		// parse info tag
		Element info = (Element) (contest.getElementsByTagName("info").item(0));
		ContestInfo cInfo = new ContestInfo(info);
		if (contestInfo == null) {
			System.out.println(cInfo);
			contestInfo = cInfo;
			// TODO: compare / warn user if data has changed
		}

		// parse problem tags
		NodeList problemTags = contest.getElementsByTagName("problem");
		if (problemTags != null) {
			for (int i = 0; i < problemTags.getLength(); i++) {
				Element problem = (Element) problemTags.item(i);
				String id = XMLHelper.getTextValue(problem, "id");
				if (id != null && !problems.contains(id)) {
					problems.add(id);
					String name = XMLHelper.getTextValue(problem, "name");
					String probTag = XMLHelper.s("problem");
					probTag += XMLHelper.simpleElement("id", id);
					probTag += XMLHelper.simpleElement("name", name);
					probTag += XMLHelper.e("problem");
					System.out.println(probTag);
				}
			}
		}

		// parse team tags
		NodeList teamTags = contest.getElementsByTagName("team");
		if (teamTags != null) {
			for (int i = 0; i < teamTags.getLength(); i++) {
				Element team = (Element) teamTags.item(i);
				String id = XMLHelper.getTextValue(team, "id");
				if (id != null && !teams.contains(id)) {
					teams.add(id);
					String name = XMLHelper.getTextValue(team, "name");
					String nationality = XMLHelper.getTextValue(team,
							"nationality");
					String university = XMLHelper.getTextValue(team,
							"university");
					String teamTag = XMLHelper.s("team");
					teamTag += XMLHelper.simpleElement("id", id);
					teamTag += XMLHelper.simpleElement("name", name);
					teamTag += XMLHelper
							.simpleElement("university", university);
					teamTag += XMLHelper.simpleElement("nationality",
							nationality);
					teamTag += XMLHelper.e("team");
					System.out.println(teamTag);
				}
			}
		}

		// parse run tags
		NodeList runTags = contest.getElementsByTagName("run");
		if (runTags != null) {
			for (int i = 0; i < runTags.getLength(); i++) {
				Element runElement = (Element) runTags.item(i);
				Run run = new Run(runElement);
				if (run.judged != null && !runs.contains(run)) {
					runs.add(run);
					System.out.println(run);
				}
			}
		}

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Document doc = parseXmlFile();
		parseDocument(doc);
	}
}
