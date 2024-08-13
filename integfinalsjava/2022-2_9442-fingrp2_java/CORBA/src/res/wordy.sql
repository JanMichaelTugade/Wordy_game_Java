-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Apr 28, 2023 at 02:44 PM
-- Server version: 8.0.31
-- PHP Version: 8.0.26

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `wordy`
--

-- --------------------------------------------------------

--
-- Table structure for table `game_session`
--

DROP TABLE IF EXISTS `game_session`;
CREATE TABLE IF NOT EXISTS `game_session` (
  `roomName` varchar(13) NOT NULL,
  `userID` bigint NOT NULL,
  `searchStatus` varchar(9) DEFAULT NULL,
  `activeStatus` varchar(7) DEFAULT NULL,
  `pointsAchieved` int DEFAULT NULL,
  `longestWord` varchar(40) DEFAULT NULL,
  KEY `userID_fk` (`userID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `game_session`
--

INSERT INTO `game_session` (`roomName`, `userID`, `searchStatus`, `activeStatus`, `pointsAchieved`, `longestWord`) VALUES
('room 1', 2, 'searching', 'pending', 1, 'NULL'),
('room 1', 5, 'searching', 'pending', 2, 'NULL');

-- --------------------------------------------------------

--
-- Table structure for table `leaderboard`
--

DROP TABLE IF EXISTS `leaderboard`;
CREATE TABLE IF NOT EXISTS `leaderboard` (
  `userID` bigint NOT NULL,
  `longestWord` text NOT NULL,
  `matchesWon` int NOT NULL,
  UNIQUE KEY `userID` (`userID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `leaderboard`
--

INSERT INTO `leaderboard` (`userID`, `longestWord`, `matchesWon`) VALUES
(1, 'ABATES', 14),
(2, 'NORTHWESTERLY', 21),
(3, 'REINED', 31),
(4, 'SMUGLY', 10),
(5, 'DRIFT', 1),
(6, 'CRUCIAL', 2);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `userID` bigint NOT NULL AUTO_INCREMENT,
  `userName` text NOT NULL,
  `password` varchar(10) NOT NULL,
  `status` varchar(10) NOT NULL DEFAULT 'OFFLINE',
  PRIMARY KEY (`userID`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`userID`, `userName`, `password`, `status`) VALUES
(1, 'rose11', '123', 'OFFLINE'),
(2, 'coolkidx', '123', 'OFFLINE'),
(3, 'newname', '123', 'OFFLINE'),
(4, 'suja', '123', 'OFFLINE'),
(5, 'roger', '123', 'OFFLINE'),
(6, 'axel', '123', 'OFFLINE'),
(7, 'zydex', '123', 'OFFLINE');

--
-- Constraints for dumped tables
--

--
-- Constraints for table `game_session`
--
ALTER TABLE `game_session`
  ADD CONSTRAINT `userID_fk` FOREIGN KEY (`userID`) REFERENCES `users` (`userID`) ON DELETE RESTRICT ON UPDATE CASCADE;

--
-- Constraints for table `leaderboard`
--
ALTER TABLE `leaderboard`
  ADD CONSTRAINT `leaderboard_ibfk_1` FOREIGN KEY (`userID`) REFERENCES `users` (`userID`) ON DELETE RESTRICT ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
