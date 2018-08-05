-- This script upgrades table structure, data, and privileges
-- from/to the exact version numbers specified in the filename.

--
-- First execute a check whether this upgrade should apply. The check
-- below should fail if this upgrade has already been applied, but
-- keep everything unchanged if not.
--

-- @UPGRADE-CHECK@
ALTER TABLE `problem` ADD  COLUMN `output_image_gen` varchar(32);
ALTER TABLE `problem` DROP COLUMN `output_image_gen`;

INSERT INTO `configuration` (`name`, `value`, `type`, `public`, `description`) VALUES
('data_source', '0', 'int', '0', 'Source of data. Choices: 0 = all local, 1 = configuration data external, 2 = configuration and live data external');

--
-- Create additional structures
--

ALTER TABLE `problem`
  ADD COLUMN `output_image_gen` varchar(32) DEFAULT NULL COMMENT 'Script to generate a graphical representation of the team output for a testcase';

--
-- Transfer data from old to new structure
--


--
-- Add/remove sample/initial contents
--


--
-- Finally remove obsolete structures after moving data
--

