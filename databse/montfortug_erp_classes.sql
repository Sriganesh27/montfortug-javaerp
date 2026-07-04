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
-- Table structure for table `erp_classes`
--

DROP TABLE IF EXISTS `erp_classes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_classes` (
  `class_id` int(11) NOT NULL AUTO_INCREMENT,
  `level_id` int(11) NOT NULL,
  `class_code` varchar(255) NOT NULL,
  `class_name` varchar(255) NOT NULL,
  `display_order` int(11) NOT NULL,
  `status` int(11) DEFAULT 1,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`class_id`),
  UNIQUE KEY `class_code` (`class_code`),
  KEY `fk_school_class_level` (`level_id`),
  CONSTRAINT `fk_school_class_level` FOREIGN KEY (`level_id`) REFERENCES `erp_levels` (`level_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_classes`
--

LOCK TABLES `erp_classes` WRITE;
/*!40000 ALTER TABLE `erp_classes` DISABLE KEYS */;
INSERT INTO `erp_classes` VALUES (1,1,'N1','Baby Class',1,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(2,1,'N2','Middle Class',2,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(3,1,'N3','Top Class',3,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(4,2,'P1','Primary 1',4,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(5,2,'P2','Primary 2',5,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(6,2,'P3','Primary 3',6,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(7,2,'P4','Primary 4',7,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(8,2,'P5','Primary 5',8,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(9,2,'P6','Primary 6',9,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(10,2,'P7','Primary 7',10,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(11,3,'S1','Secondary 1',11,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(12,3,'S2','Secondary 2',12,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(13,3,'S3','Secondary 3',13,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(14,3,'S4','Secondary 4',14,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(15,4,'S5','Senior Secondary 5',15,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(16,4,'S6','Senior Secondary 6',16,1,'2026-06-29 14:52:00','2026-06-29 14:52:00');
/*!40000 ALTER TABLE `erp_classes` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-04 14:11:55
