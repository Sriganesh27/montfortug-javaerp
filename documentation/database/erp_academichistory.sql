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
-- Table structure for table `erp_academichistory`
--

DROP TABLE IF EXISTS `erp_academichistory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_academichistory` (
  `HistoryID` int(11) NOT NULL AUTO_INCREMENT,
  `AdmissionNo` int(11) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `FormerSchool` varchar(255) DEFAULT NULL,
  `LIN` varchar(100) DEFAULT NULL,
  `Combination` varchar(100) DEFAULT NULL,
  `PLEIndexNumber` varchar(100) DEFAULT NULL,
  `PLEAggregate` varchar(50) DEFAULT NULL,
  `UCEIndexNumber` varchar(100) DEFAULT NULL,
  `UCEResult` varchar(100) DEFAULT NULL,
  `FormerSchoolCode` varchar(50) DEFAULT NULL,
  `SubjectMarks` longtext DEFAULT NULL,
  `PreviousMarksDoc` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`HistoryID`),
  KEY `branch_id` (`branch_id`,`AdmissionNo`),
  CONSTRAINT `erp_academichistory_ibfk_1` FOREIGN KEY (`branch_id`, `AdmissionNo`) REFERENCES `erp_students` (`branch_id`, `AdmissionNo`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_academichistory`
--

LOCK TABLES `erp_academichistory` WRITE;
/*!40000 ALTER TABLE `erp_academichistory` DISABLE KEYS */;
INSERT INTO `erp_academichistory` VALUES (1,1,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'[]',NULL),(2,2,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'[]',NULL),(3,3,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'[]',NULL),(4,4,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'[]',NULL),(5,5,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'[]',NULL),(6,6,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'[]',NULL),(9,7,1,'hll','hh',NULL,'','','','','788','[{\"mark\":\"89\",\"grade\":\"A\",\"name\":\"Physics\"}]',NULL),(10,8,1,'hll','hh',NULL,'','','','','788','[{\"mark\":\"89\",\"grade\":\"A\",\"name\":\"Physics\"}]',NULL),(11,9,1,'hll','hh',NULL,'','','','','788','[{\"mark\":\"89\",\"grade\":\"A\",\"name\":\"Physics\"}]',NULL),(12,10,1,'hll','hh',NULL,'','','','','788','[]','/assets/uploads/students/0010_SRI_SUDHAGANI/SRI_SUDHAGANI_10_prev_marks.jpeg');
/*!40000 ALTER TABLE `erp_academichistory` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-09 19:47:13
