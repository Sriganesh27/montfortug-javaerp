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
-- Table structure for table `erp_application_status_history`
--

DROP TABLE IF EXISTS `erp_application_status_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_application_status_history` (
  `history_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) NOT NULL,
  `old_status` varchar(30) DEFAULT NULL,
  `new_status` varchar(30) NOT NULL,
  `changed_by` bigint(20) DEFAULT NULL,
  `changed_at` datetime DEFAULT current_timestamp(),
  `remarks` text DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`history_id`),
  KEY `fk_application_status_history_application` (`application_id`),
  CONSTRAINT `fk_application_status_history_application` FOREIGN KEY (`application_id`) REFERENCES `erp_applications` (`application_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_application_status_history`
--

LOCK TABLES `erp_application_status_history` WRITE;
/*!40000 ALTER TABLE `erp_application_status_history` DISABLE KEYS */;
INSERT INTO `erp_application_status_history` VALUES (1,1,NULL,'SUBMITTED',NULL,'2026-07-01 09:44:32','Application submitted by user',1,0),(2,2,NULL,'SUBMITTED',NULL,'2026-07-01 09:54:17','Application submitted by user',1,0),(3,3,NULL,'SUBMITTED',NULL,'2026-07-01 10:02:58','Application submitted by user',1,0),(4,4,NULL,'SUBMITTED',NULL,'2026-07-01 10:07:18','Application submitted by user',1,0),(5,5,NULL,'SUBMITTED',NULL,'2026-07-01 10:32:19','Application submitted by user',1,0),(6,6,NULL,'SUBMITTED',NULL,'2026-07-01 12:00:45','Application submitted by user',1,0),(7,7,NULL,'SUBMITTED',NULL,'2026-07-01 12:24:40','Application submitted by user',1,0),(8,8,NULL,'SUBMITTED',NULL,'2026-07-01 12:37:37','Application submitted by user',1,0),(9,9,NULL,'SUBMITTED',NULL,'2026-07-01 13:24:50','Application submitted by user',1,0),(10,10,NULL,'SUBMITTED',NULL,'2026-07-02 07:54:55','Application submitted by user',1,0),(11,11,NULL,'SUBMITTED',NULL,'2026-07-02 08:10:26','Application submitted by user',1,0),(12,12,NULL,'SUBMITTED',NULL,'2026-07-02 09:24:02','Application submitted by user',1,0),(13,13,NULL,'SUBMITTED',NULL,'2026-07-02 09:24:11','Application submitted by user',1,0),(14,14,NULL,'SUBMITTED',NULL,'2026-07-02 15:15:53','Application submitted by user',1,0),(15,15,NULL,'SUBMITTED',NULL,'2026-07-02 15:39:42','Application submitted by user',1,0),(16,16,NULL,'SUBMITTED',NULL,'2026-07-02 17:28:33','Application submitted by user',1,0),(17,17,NULL,'SUBMITTED',NULL,'2026-07-03 09:58:26','Application submitted by user',1,0),(18,18,NULL,'SUBMITTED',NULL,'2026-07-03 10:27:24','Application submitted by user',1,0);
/*!40000 ALTER TABLE `erp_application_status_history` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-11  0:29:51
