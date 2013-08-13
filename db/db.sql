SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `eve_finger` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `eve_finger` ;

-- -----------------------------------------------------
-- Table `eve_finger`.`tblAccessGroups`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `eve_finger`.`tblAccessGroups` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(45) NOT NULL ,
  `power` INT NOT NULL ,
  `notes` TEXT NULL ,
  `visible` TINYINT(1) NOT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `eve_finger`.`tblUsers`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `eve_finger`.`tblUsers` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `charID` BIGINT NULL ,
  `username` VARCHAR(45) NOT NULL ,
  `accessGroup` INT NOT NULL ,
  `pwHash` CHAR(64) NULL ,
  `lastLogin` DATETIME NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `charID_UNIQUE` (`charID` ASC) ,
  UNIQUE INDEX `username_UNIQUE` (`username` ASC) ,
  INDEX `FK_tblUsers_tblAccessGroups` (`accessGroup` ASC) ,
  CONSTRAINT `FK_tblUsers_tblAccessGroups`
    FOREIGN KEY (`accessGroup` )
    REFERENCES `eve_finger`.`tblAccessGroups` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `eve_finger`.`tblSessions`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `eve_finger`.`tblSessions` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `sessionID` CHAR(36) NOT NULL ,
  `userID` INT NOT NULL ,
  `createdAt` DATETIME NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `sessionID_UNIQUE` (`sessionID` ASC) ,
  INDEX `FK_tblSessions_tblUsers` (`userID` ASC) ,
  CONSTRAINT `FK_tblSessions_tblUsers`
    FOREIGN KEY (`userID` )
    REFERENCES `eve_finger`.`tblUsers` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `eve_finger`.`tblAlliances`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `eve_finger`.`tblAlliances` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `allianceID` BIGINT NOT NULL ,
  `allianceName` VARCHAR(45) NOT NULL ,
  `neededAccess` INT NOT NULL ,
  `notes` TEXT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `allianceID_UNIQUE` (`allianceID` ASC) ,
  UNIQUE INDEX `allianceName_UNIQUE` (`allianceName` ASC) ,
  INDEX `FK_tblAlliances_tblAccessGroups` (`neededAccess` ASC) ,
  CONSTRAINT `FK_tblAlliances_tblAccessGroups`
    FOREIGN KEY (`neededAccess` )
    REFERENCES `eve_finger`.`tblAccessGroups` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `eve_finger`.`tblCorps`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `eve_finger`.`tblCorps` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `corpID` BIGINT NOT NULL ,
  `corpName` VARCHAR(45) NOT NULL ,
  `allianceID` BIGINT NOT NULL ,
  `neededAccess` INT NOT NULL ,
  `notes` TEXT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `corpID_UNIQUE` (`corpID` ASC) ,
  UNIQUE INDEX `corpName_UNIQUE` (`corpName` ASC) ,
  INDEX `FK_tblCorps_tblAlliances` (`allianceID` ASC) ,
  INDEX `FK_tblCorps_tblAccessGroups` (`neededAccess` ASC) ,
  CONSTRAINT `FK_tblCorps_tblAlliances`
    FOREIGN KEY (`allianceID` )
    REFERENCES `eve_finger`.`tblAlliances` (`allianceID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_tblCorps_tblAccessGroups`
    FOREIGN KEY (`neededAccess` )
    REFERENCES `eve_finger`.`tblAccessGroups` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `eve_finger`.`tblCharacters`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `eve_finger`.`tblCharacters` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `charName` VARCHAR(24) NOT NULL ,
  `charID` BIGINT NOT NULL ,
  `allianceID` BIGINT NOT NULL ,
  `corpID` BIGINT NOT NULL ,
  `assocWithAlliance` BIGINT NULL ,
  `timezone` VARCHAR(45) NULL ,
  `neededAccess` INT NOT NULL ,
  `isSuper` TINYINT(1) NOT NULL ,
  `isTitan` TINYINT(1) NOT NULL ,
  `isFC` TINYINT(1) NOT NULL ,
  `isScout` TINYINT(1) NOT NULL ,
  `isCyno` TINYINT(1) NOT NULL ,
  `cachedUntil` DATETIME NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `charName_UNIQUE` (`charName` ASC) ,
  UNIQUE INDEX `charID_UNIQUE` (`charID` ASC) ,
  INDEX `FK_tblCharacters_tblAccessGroups` (`neededAccess` ASC) ,
  INDEX `FK_tblCharacters_tblAlliances` (`allianceID` ASC) ,
  INDEX `FK_tblCharacters_tblCorps` (`corpID` ASC) ,
  INDEX `FK_tblCharacters_Assoc_tblAlliances` (`assocWithAlliance` ASC) ,
  CONSTRAINT `FK_tblCharacters_tblAccessGroups`
    FOREIGN KEY (`neededAccess` )
    REFERENCES `eve_finger`.`tblAccessGroups` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_tblCharacters_tblAlliances`
    FOREIGN KEY (`allianceID` )
    REFERENCES `eve_finger`.`tblAlliances` (`allianceID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_tblCharacters_tblCorps`
    FOREIGN KEY (`corpID` )
    REFERENCES `eve_finger`.`tblCorps` (`corpID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_tblCharacters_Assoc_tblAlliances`
    FOREIGN KEY (`assocWithAlliance` )
    REFERENCES `eve_finger`.`tblAlliances` (`allianceID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `eve_finger`.`tblCharNotes`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `eve_finger`.`tblCharNotes` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `charID` BIGINT NOT NULL ,
  `neededAccess` INT NOT NULL ,
  `addedBy` INT NOT NULL ,
  `notes` TEXT NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK_tblCharNotes_tblAccessGroups` (`neededAccess` ASC) ,
  INDEX `FK_tblCharNotes_tblCharacters` (`charID` ASC) ,
  INDEX `FK_tblCharNotes_tblUsers` (`addedBy` ASC) ,
  CONSTRAINT `FK_tblCharNotes_tblAccessGroups`
    FOREIGN KEY (`neededAccess` )
    REFERENCES `eve_finger`.`tblAccessGroups` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_tblCharNotes_tblCharacters`
    FOREIGN KEY (`charID` )
    REFERENCES `eve_finger`.`tblCharacters` (`charID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_tblCharNotes_tblUsers`
    FOREIGN KEY (`addedBy` )
    REFERENCES `eve_finger`.`tblUsers` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `eve_finger`.`tblLog`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `eve_finger`.`tblLog` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `userID` INT NOT NULL ,
  `created` DATETIME NOT NULL ,
  `type` VARCHAR(45) NOT NULL ,
  `message` TEXT NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK_tblLog_tblUsers` (`userID` ASC) ,
  INDEX `tblLog_created_Index` USING BTREE (`created` ASC) ,
  CONSTRAINT `FK_tblLog_tblUsers`
    FOREIGN KEY (`userID` )
    REFERENCES `eve_finger`.`tblUsers` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Placeholder table for view `eve_finger`.`viewSessionList`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `eve_finger`.`viewSessionList` (`username` INT, `userID` INT, `sessionID` INT, `createdAt` INT, `accessGroup` INT);

-- -----------------------------------------------------
-- View `eve_finger`.`viewSessionList`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `eve_finger`.`viewSessionList`;
USE `eve_finger`;
CREATE  OR REPLACE VIEW `eve_finger`.`viewSessionList` AS

SELECT u.username AS username, s.userID AS userID, s.sessionID AS sessionID, s.createdAt AS createdAt, u.accessGroup AS accessGroup

FROM tblSessions AS s INNER JOIN tblUsers AS u ON (s.userID = u.id);
;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `eve_finger`.`tblAccessGroups`
-- -----------------------------------------------------
START TRANSACTION;
USE `eve_finger`;
INSERT INTO `eve_finger`.`tblAccessGroups` (`id`, `name`, `power`, `notes`, `visible`) VALUES (1, 'No Access', 0, NULL, 0);

COMMIT;

-- -----------------------------------------------------
-- Data for table `eve_finger`.`tblAlliances`
-- -----------------------------------------------------
START TRANSACTION;
USE `eve_finger`;
INSERT INTO `eve_finger`.`tblAlliances` (`id`, `allianceID`, `allianceName`, `neededAccess`, `notes`) VALUES (0, 0, 'None', 4, NULL);

COMMIT;
