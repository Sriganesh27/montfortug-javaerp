-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 68.178.237.26    Database: montfortug
-- ------------------------------------------------------
-- Server version	5.5.5-10.11.17-MariaDB-cll-lve

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
-- Table structure for table `erp_branches`
--

DROP TABLE IF EXISTS `erp_branches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_branches` (
  `branch_id` int(11) NOT NULL AUTO_INCREMENT,
  `branch_name` varchar(255) NOT NULL,
  `school_code` varchar(10) DEFAULT NULL,
  `branch_location` varchar(255) DEFAULT NULL,
  `is_active` int(11) DEFAULT 1,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `contact_details` text DEFAULT NULL,
  `foundation_date` varchar(255) DEFAULT NULL,
  `gov_document_url` varchar(255) DEFAULT NULL,
  `incharge_details` text DEFAULT NULL,
  `school_photo_url` varchar(255) DEFAULT NULL,
  `branch_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_branches`
--

LOCK TABLES `erp_branches` WRITE;
/*!40000 ALTER TABLE `erp_branches` DISABLE KEYS */;
INSERT INTO `erp_branches` VALUES (1,'St. Kizito Nursery & Primary School','U011','Kyebando',1,'2026-06-25 08:43:09.000000',NULL,'2026-06-25 08:43:09.000000',NULL,'-','2024-05-25',NULL,'[]',NULL,NULL),(2,'St.Montfort Nursery & Primary School','U021','Mpala',1,'2026-06-25 08:45:46.000000',NULL,'2026-06-25 08:45:46.000000',NULL,'-','2022-04-25',NULL,'[]',NULL,NULL),(3,'Pere Achte Secondary & Senior Secondary School','U031','Isunga',1,'2026-06-25 08:46:45.000000',NULL,'2026-06-25 08:46:45.000000',NULL,'-','2022-04-25',NULL,'[]',NULL,NULL),(4,'St.Montfort Nursery & Primary School','U032','Isunga',1,'2026-07-09 11:55:06.000000','1','2026-07-09 11:55:06.000000','1','22348888988',NULL,NULL,'[]',NULL,NULL),(5,'St.Montfort Nursery & Primary School','U032','Isunga',1,'2026-07-09 11:56:54.000000','1','2026-07-09 11:56:54.000000','1','22348888988',NULL,NULL,'[]',NULL,NULL),(6,'St.Montfort Nursery & Primary School','U032','Isunga',1,'2026-07-09 12:02:55.000000','1','2026-07-09 12:02:55.000000','1','22348888988',NULL,NULL,'[]',NULL,NULL),(7,'St.Montfort Nursery & Primary School','U032','Isunga',1,'2026-07-09 12:10:25.000000','1','2026-07-09 12:19:58.000000','1','22348888988',NULL,NULL,'[]',NULL,NULL),(8,'St.Montfort Nursery & Primary School','U033','Isunga',1,'2026-07-09 12:21:32.000000','1','2026-07-09 12:21:32.000000','1','22348888988',NULL,NULL,'[{\"name\":\"f\",\"role\":\"ff\",\"phone\":\"6888\"}]',NULL,NULL),(9,'St.Montfort Nursery & Primary School','U034','Isunga',1,'2026-07-10 06:03:58.000000','1','2026-07-10 07:32:25.000000','1','22348888988',NULL,NULL,'[{\"name\":\"Hello\",\"role\":\"hello\",\"phone\":\"789456122\"}]','/uploads/branchdetails/U034-St.Montfort_Nursery___Primary_School-Isunga/photo/1783668745418_Screenshot24.jpeg',NULL);
/*!40000 ALTER TABLE `erp_branches` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-11  0:29:41
