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
-- Table structure for table `web_audit_logs`
--

DROP TABLE IF EXISTS `web_audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `web_audit_logs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `admin_id` int(11) NOT NULL,
  `action_type` varchar(50) NOT NULL,
  `target_table` varchar(50) NOT NULL,
  `target_id` int(11) NOT NULL,
  `ip_address` varchar(45) NOT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `web_audit_logs`
--

LOCK TABLES `web_audit_logs` WRITE;
/*!40000 ALTER TABLE `web_audit_logs` DISABLE KEYS */;
INSERT INTO `web_audit_logs` VALUES (1,1,'DELETE_RECORD','web_contact_inquiries',53,'223.185.45.38','2026-03-25 06:39:42'),(2,1,'DELETE_RECORD','web_contact_inquiries',60,'223.185.45.38','2026-03-25 09:38:03'),(3,1,'DELETE_RECORD','web_contact_inquiries',59,'223.185.45.38','2026-03-25 09:46:19'),(4,1,'DELETE_RECORD','web_contact_inquiries',58,'223.185.45.38','2026-03-25 09:46:22'),(5,1,'DELETE_RECORD','web_contact_inquiries',57,'223.185.45.38','2026-03-25 09:46:26'),(6,1,'DELETE_RECORD','web_contact_inquiries',56,'223.185.45.38','2026-03-25 09:46:29'),(7,1,'DELETE_RECORD','web_contact_inquiries',55,'223.185.45.38','2026-03-25 09:46:32'),(8,1,'DELETE_RECORD','web_contact_inquiries',54,'223.185.45.38','2026-03-25 09:46:34'),(9,1,'DELETE_RECORD','web_volunteer_form',4,'223.185.45.38','2026-03-25 09:47:18'),(10,1,'DELETE_RECORD','web_volunteer_form',3,'223.185.45.38','2026-03-25 09:47:20'),(11,1,'DELETE_RECORD','web_volunteer_form',2,'223.185.45.38','2026-03-25 09:47:25'),(12,1,'DELETE_RECORD','web_volunteer_form',1,'223.185.45.38','2026-03-25 09:47:27'),(13,1,'DELETE_RECORD','web_contact_inquiries',62,'::1','2026-03-26 03:21:30'),(14,1,'DELETE_RECORD','web_contact_inquiries',61,'::1','2026-03-26 03:21:33'),(15,1,'DELETE_RECORD','web_contact_inquiries',69,'106.200.28.213','2026-03-26 03:32:08'),(16,1,'DELETE_RECORD','web_contact_inquiries',68,'106.200.28.213','2026-03-26 03:32:10'),(17,1,'DELETE_RECORD','web_contact_inquiries',67,'106.200.28.213','2026-03-26 03:32:13'),(18,1,'DELETE_RECORD','web_contact_inquiries',66,'106.200.28.213','2026-03-26 03:32:17'),(19,1,'DELETE_RECORD','web_contact_inquiries',65,'106.200.28.213','2026-03-26 03:32:20'),(20,1,'DELETE_RECORD','web_contact_inquiries',64,'106.200.28.213','2026-03-26 03:32:22'),(21,1,'DELETE_RECORD','web_contact_inquiries',63,'106.200.28.213','2026-03-26 03:32:25'),(22,1,'DELETE_RECORD','web_contact_inquiries',71,'106.200.28.213','2026-03-26 03:41:08'),(23,1,'DELETE_RECORD','web_contact_inquiries',70,'106.200.28.213','2026-03-26 03:41:11'),(24,1,'DELETE_RECORD','web_contact_inquiries',72,'106.200.28.213','2026-03-26 03:42:29'),(25,1,'DELETE_RECORD','web_contact_inquiries',79,'103.42.201.122','2026-03-31 22:04:46'),(26,1,'DELETE_RECORD','web_contact_inquiries',78,'103.42.201.122','2026-03-31 22:04:48'),(27,1,'DELETE_RECORD','web_contact_inquiries',77,'103.42.201.122','2026-03-31 22:04:51'),(28,1,'DELETE_RECORD','web_contact_inquiries',76,'103.42.201.122','2026-03-31 22:04:53'),(29,1,'DELETE_RECORD','web_contact_inquiries',75,'103.42.201.122','2026-03-31 22:04:55'),(30,1,'DELETE_RECORD','web_contact_inquiries',74,'103.42.201.122','2026-03-31 22:04:58'),(31,1,'DELETE_RECORD','web_contact_inquiries',73,'103.42.201.122','2026-03-31 22:05:02'),(32,1,'DELETE_RECORD','web_volunteer_form',12,'103.42.201.122','2026-03-31 22:05:07'),(33,1,'DELETE_RECORD','web_volunteer_form',11,'103.42.201.122','2026-03-31 22:05:11'),(34,1,'DELETE_RECORD','web_volunteer_form',10,'103.42.201.122','2026-03-31 22:05:17'),(35,1,'DELETE_RECORD','web_contact_inquiries',81,'106.222.233.6','2026-04-15 02:45:24'),(36,1,'DELETE_RECORD','web_contact_inquiries',83,'106.222.233.6','2026-04-15 03:47:32'),(37,1,'DELETE_RECORD','web_contact_inquiries',82,'106.222.233.6','2026-04-15 03:47:36'),(38,1,'DELETE_RECORD','web_contact_inquiries',85,'106.222.233.6','2026-04-15 03:55:25'),(39,1,'DELETE_RECORD','web_contact_inquiries',84,'106.222.233.6','2026-04-15 03:55:28'),(40,1,'DELETE_RECORD','web_contact_inquiries',86,'::1','2026-04-15 06:15:21'),(41,1,'DELETE_RECORD','web_volunteer_form',5,'::1','2026-04-15 06:15:28'),(42,1,'DELETE_RECORD','web_volunteer_form',4,'::1','2026-04-15 06:15:31'),(43,1,'DELETE_RECORD','web_volunteer_form',3,'::1','2026-04-15 06:15:34'),(44,1,'DELETE_RECORD','web_volunteer_form',2,'::1','2026-04-15 06:15:37'),(45,1,'DELETE_RECORD','web_volunteer_form',1,'::1','2026-04-15 06:15:40'),(46,1,'DELETE_RECORD','web_contact_inquiries',89,'103.42.201.122','2026-04-17 09:55:44'),(47,1,'DELETE_RECORD','web_contact_inquiries',90,'103.42.201.122','2026-04-17 09:55:47'),(48,1,'DELETE_RECORD','web_contact_inquiries',88,'103.42.201.122','2026-04-17 09:55:49'),(49,1,'DELETE_RECORD','web_contact_inquiries',87,'103.42.201.122','2026-04-17 09:55:53'),(50,1,'DELETE_RECORD','web_contact_inquiries',92,'103.216.221.101','2026-04-17 11:38:19'),(51,1,'DELETE_RECORD','web_contact_inquiries',91,'103.216.221.101','2026-04-17 11:38:22'),(52,1,'DELETE_RECORD','web_volunteer_form',8,'103.216.221.101','2026-04-17 11:38:29'),(53,1,'DELETE_RECORD','web_volunteer_form',9,'103.216.221.101','2026-04-17 11:38:33'),(54,1,'DELETE_RECORD','web_volunteer_form',6,'103.216.221.101','2026-04-17 11:38:35'),(55,1,'DELETE_RECORD','web_volunteer_form',7,'103.216.221.101','2026-04-17 11:38:38'),(56,1,'DELETE_RECORD','web_users',2,'122.183.36.85','2026-06-01 10:01:04');
/*!40000 ALTER TABLE `web_audit_logs` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-20 13:32:45
