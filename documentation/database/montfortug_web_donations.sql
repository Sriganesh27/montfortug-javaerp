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
-- Table structure for table `web_donations`
--

DROP TABLE IF EXISTS `web_donations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `web_donations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `receipt_number` varchar(20) DEFAULT NULL,
  `full_name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `contact_number` varchar(50) NOT NULL,
  `location` varchar(255) NOT NULL,
  `is_anonymous` tinyint(1) DEFAULT 0,
  `contribution_purpose` varchar(100) NOT NULL,
  `project_id` varchar(20) DEFAULT NULL,
  `currency` varchar(20) NOT NULL,
  `amount` decimal(15,2) DEFAULT 0.00,
  `amount_received` decimal(15,2) DEFAULT NULL,
  `amount_spent` decimal(15,2) DEFAULT 0.00,
  `payment_status` enum('success','failed') NOT NULL,
  `transaction_id` varchar(100) DEFAULT NULL,
  `failure_reason` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `students_benefited` int(11) DEFAULT 0,
  `terms_benefited` int(11) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `web_donations`
--

LOCK TABLES `web_donations` WRITE;
/*!40000 ALTER TABLE `web_donations` DISABLE KEYS */;
INSERT INTO `web_donations` VALUES (1,'SSP001-001','Dennis Rodricks','dennisrodricks@yahoo.com','+1 ','Florida USA',0,'Scholarships for student','SSP001','USD',300.00,1058972.00,0.00,'success','R454886400657','','2026-03-09 11:14:39',5,1),(2,'SSP001-002','Dennis Rodricks','','+1 7727669985','St Vero Beach,FL',0,'Scholarships','SSP001','USD',800.00,2820132.00,0.00,'success','R617788957276','','2026-05-04 04:55:46',13,1),(3,'SSP001-003','Gopi Manchineella','Gopi.manchi@gmail.com','+1 5137208610','Virginia Beach, USA',1,'Scholarships for Students','SSP001','USD',150.00,NULL,0.00,'success','X4NGXPEH','','2026-05-23 19:38:10',0,0);
/*!40000 ALTER TABLE `web_donations` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-20 13:33:12
