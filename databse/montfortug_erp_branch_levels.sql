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
-- Table structure for table `erp_branch_levels`
--

DROP TABLE IF EXISTS `erp_branch_levels`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_branch_levels` (
  `branch_level_id` int(11) NOT NULL AUTO_INCREMENT,
  `branch_id` int(11) NOT NULL,
  `level_id` int(11) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `created_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`branch_level_id`),
  UNIQUE KEY `uk_branch_level` (`branch_id`,`level_id`),
  UNIQUE KEY `UKsac1pi66it0u88um2v2yfkwrq` (`branch_id`,`level_id`),
  KEY `fk_branch_level_level` (`level_id`),
  CONSTRAINT `fk_branch_level_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_branch_level_level` FOREIGN KEY (`level_id`) REFERENCES `erp_levels` (`level_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_branch_levels`
--

LOCK TABLES `erp_branch_levels` WRITE;
/*!40000 ALTER TABLE `erp_branch_levels` DISABLE KEYS */;
INSERT INTO `erp_branch_levels` VALUES (1,1,1,'2026-06-29 14:35:26',NULL),(2,2,1,'2026-06-29 14:35:26',NULL),(5,2,2,'2026-06-29 14:35:26',NULL),(7,3,3,'2026-06-29 14:35:27',NULL),(9,3,4,'2026-06-29 23:26:48','SUPER_ADMIN'),(10,1,2,'2026-06-30 01:25:57','SUPER_ADMIN');
/*!40000 ALTER TABLE `erp_branch_levels` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-04 14:11:59
