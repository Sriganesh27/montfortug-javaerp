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
-- Table structure for table `erp_users`
--

DROP TABLE IF EXISTS `erp_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(50) DEFAULT NULL,
  `assigned_branch` int(11) DEFAULT NULL,
  `is_active` int(11) DEFAULT 1,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `assigned_branch` (`assigned_branch`),
  CONSTRAINT `erp_users_ibfk_1` FOREIGN KEY (`assigned_branch`) REFERENCES `erp_branches` (`branch_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_users`
--

LOCK TABLES `erp_users` WRITE;
/*!40000 ALTER TABLE `erp_users` DISABLE KEYS */;
INSERT INTO `erp_users` VALUES (1,'erpsadmin','$2a$10$7Ny6SkTXvs4g6Vu3C.avUuQ5rtgt4iQk55NZQI2Lb73R3MqocMMuC','SUPER_ADMIN',NULL,1,NULL,NULL,'2026-06-16 13:05:24.000000',NULL),(2,'newuser','$2a$10$7cH2IT9DIGhcQqQuyEGf..GMOEsT2Zu6qspsvXLUp64Fgc9VejO9e','SUPER_ADMIN',NULL,1,'2026-06-16 13:05:23.000000',NULL,'2026-06-16 13:05:23.000000',NULL),(3,'u011@montfort.ug','$2a$10$DeZW/Yc2xrCYKhgB7IUd4uKeLPJwCt5yI4km2lifmvTqa6bTTf2Xm','School Admin',2,1,'2026-06-16 15:02:52.000000',NULL,'2026-06-16 15:02:52.000000',NULL),(4,'u021@montfort.ug','$2a$10$Im8ewQLYWa.58vB0Bk.qP.zZAuXQ4PwCYNRDBb94Oo6F99dADm0eG','School Admin',3,1,'2026-06-16 15:32:33.000000',NULL,'2026-06-16 15:32:33.000000',NULL),(5,'u031@montfort.ug','$2a$10$dY7YH8qTit93bOfJoVonrelKtQLu.1isGoJdMnuAk6vvcaOePqKLW','School Admin',4,1,'2026-06-16 16:16:05.000000',NULL,'2026-06-16 16:16:05.000000',NULL);
/*!40000 ALTER TABLE `erp_users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-20 13:33:06
