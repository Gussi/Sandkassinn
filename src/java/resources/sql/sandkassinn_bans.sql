CREATE TABLE IF NOT EXISTS `sandkassinn_bans` (
  `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Unique ID',
  `banned` VARCHAR(63) NOT NULL COMMENT 'Player name',
  `type` ENUM('tempban','permaban','warning','pardon') NOT NULL COMMENT 'Type of action',
  `reason` VARCHAR(255) NOT NULL COMMENT 'Given reason',
  `executor` VARCHAR(63) NOT NULL COMMENT 'Name of executor',
  `date_executed` INT(10) UNSIGNED NOT NULL COMMENT 'Execution date',
  `date_expire` INT(10) UNSIGNED NOT NULL COMMENT 'Expire date',
  PRIMARY KEY (`id`)
) ENGINE=MYISAM DEFAULT CHARSET=utf8;