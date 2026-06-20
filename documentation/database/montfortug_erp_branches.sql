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
-- Table structure for table `erp_branches`
--

DROP TABLE IF EXISTS `erp_branches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_branches` (
  `branch_id` int(11) NOT NULL AUTO_INCREMENT,
  `branch_name` varchar(255) NOT NULL,
  `school_code` varchar(10) DEFAULT NULL,
  `branch_type` varchar(255) DEFAULT NULL,
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
  PRIMARY KEY (`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_branches`
--

LOCK TABLES `erp_branches` WRITE;
/*!40000 ALTER TABLE `erp_branches` DISABLE KEYS */;
INSERT INTO `erp_branches` VALUES (1,'Updated Branch',NULL,NULL,NULL,0,'2026-06-16 13:05:18.000000',NULL,'2026-06-16 13:05:21.000000',NULL,NULL,NULL,NULL,NULL,NULL),(2,'St.Kizito Nursery & Primary School','U011','Nursery, Primary','Kyebando',1,'2026-06-16 15:02:52.000000',NULL,'2026-06-16 15:02:52.000000',NULL,'957215555','2022-02-16',NULL,'[]',NULL),(3,'St.Montfort Nursery & Primary School','U021','Nursery, Primary','Mpala',1,'2026-06-16 15:32:32.000000',NULL,'2026-06-16 15:32:32.000000',NULL,'22348888988','2023-01-09',NULL,'[]',NULL),(4,'Pere Achte Senior Secondary School','U031','Primary, Secondary','Isunga',1,'2026-06-16 16:16:04.000000',NULL,'2026-06-16 16:16:04.000000',NULL,'22348888988','2022-05-12',NULL,'[]',NULL),(5,'Pere Achte Senior Secondary School','U031','Primary, Secondary','Isunga',1,'2026-06-16 16:16:26.000000',NULL,'2026-06-16 16:16:26.000000',NULL,'22348888988','2022-05-12',NULL,'[]',NULL),(6,'Pere Achte Senior Secondary School','U031','Primary, Secondary','Isunga',1,'2026-06-16 16:19:18.000000',NULL,'2026-06-16 16:19:18.000000',NULL,'22348888988','2022-05-12',NULL,'[]',NULL),(7,'Pere Achte Senior Secondary School','U031','Primary, Secondary','Isunga',1,'2026-06-16 16:19:31.000000',NULL,'2026-06-16 16:19:31.000000',NULL,'22348888988','2022-05-12',NULL,'[]',NULL);
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

-- Dump completed on 2026-06-20 13:32:38
