-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 68.178.237.26    Database: montfortug
-- ------------------------------------------------------
-- Server version	5.5.5-10.11.16-MariaDB-cll-lve

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `erp_student_archives`
--

DROP TABLE IF EXISTS `erp_student_archives`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_archives` (
  `archive_id` int(11) NOT NULL AUTO_INCREMENT,
  `AdmissionNo` int(11) NOT NULL,
  `StudentID` varchar(50) DEFAULT NULL,
  `branch_id` int(11) NOT NULL,
  `AdmissionYear` int(11) DEFAULT NULL,
  `Name` varchar(100) DEFAULT NULL,
  `MiddleName` varchar(100) DEFAULT NULL,
  `Surname` varchar(100) NOT NULL,
  `DateOfBirth` date DEFAULT NULL,
  `gender` varchar(10) DEFAULT NULL,
  `Nationality` varchar(100) DEFAULT 'Ugandan',
  `Status` varchar(50) DEFAULT 'Archived',
  `archive_reason` varchar(100) NOT NULL COMMENT 'e.g., Transferred, Graduated, Dropped Out',
  `date_of_leaving` date NOT NULL,
  `comments` text DEFAULT NULL,
  `archived_by_user_id` int(11) DEFAULT NULL COMMENT 'The admin who archived this record',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`archive_id`),
  KEY `branch_id` (`branch_id`),
  KEY `AdmissionNo` (`AdmissionNo`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_archives`
--

LOCK TABLES `erp_student_archives` WRITE;
/*!40000 ALTER TABLE `erp_student_archives` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_archives` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-20 13:33:16
