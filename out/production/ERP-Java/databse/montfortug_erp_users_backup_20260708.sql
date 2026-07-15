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
-- Table structure for table `erp_users_backup_20260708`
--

DROP TABLE IF EXISTS `erp_users_backup_20260708`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_users_backup_20260708` (
  `id` int(11) NOT NULL DEFAULT 0,
  `username` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(50) DEFAULT NULL,
  `assigned_branch` int(11) DEFAULT NULL,
  `is_active` int(11) DEFAULT 1,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_users_backup_20260708`
--

LOCK TABLES `erp_users_backup_20260708` WRITE;
/*!40000 ALTER TABLE `erp_users_backup_20260708` DISABLE KEYS */;
INSERT INTO `erp_users_backup_20260708` VALUES (1,'erpsadmin','$2a$10$95vGGFy8A47oEfW0F.DDe.mlC4LceWJXhFeXBRWp7ECXnEPPotct6','SUPER_ADMIN',NULL,1,'2026-06-25 08:42:22.000000',NULL,'2026-06-25 08:42:22.000000',NULL),(2,'u011@montfort.ug','$2a$10$1Kxi4QIxq7np1CycnakyLeK5SuH44cgkRUL1ZG2FyiPopJ02RfNiK','School Admin',1,1,'2026-06-25 08:43:11.000000',NULL,'2026-06-25 08:43:11.000000',NULL),(3,'u021@montfort.ug','$2a$10$ltJEDZUSj9WL5o3PYLB9k.8xJYUtg2sGBwNTDg/RTQYYnrYDyy6/C','School Admin',2,1,'2026-06-25 08:45:46.000000',NULL,'2026-06-25 08:45:46.000000',NULL),(4,'u031@montfort.ug','$2a$10$YYFEuPbY2quMsZAaWyFJneR5I0gdwXoCKXyjufpe5jRq59l1h.zDK','School Admin',3,1,'2026-06-25 08:46:46.000000',NULL,'2026-06-25 08:46:46.000000',NULL);
/*!40000 ALTER TABLE `erp_users_backup_20260708` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-14 12:47:19
