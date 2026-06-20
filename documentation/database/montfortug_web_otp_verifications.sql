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
-- Table structure for table `web_otp_verifications`
--

DROP TABLE IF EXISTS `web_otp_verifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `web_otp_verifications` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(100) NOT NULL,
  `otp_hash` varchar(255) NOT NULL,
  `expiry` datetime NOT NULL,
  `is_verified` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `email` (`email`,`expiry`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `web_otp_verifications`
--

LOCK TABLES `web_otp_verifications` WRITE;
/*!40000 ALTER TABLE `web_otp_verifications` DISABLE KEYS */;
INSERT INTO `web_otp_verifications` VALUES (3,'sudhaganisriganesh@gmail.com','$2y$10$k9kbj0P4cv48JgiCEFkmH.37kIPrreBWdzQoCLEYg9MF2fWUPXtia','2026-03-03 11:36:23',0,'2026-03-03 11:26:23'),(4,'sudhaganisriganesh@gmail.com','$2y$10$g6w.xMysD8yzLXdLxUJk..oLtKmTacV2YIMDNA9XduKkKZFY1n9WS','2026-03-03 12:01:03',0,'2026-03-03 11:51:03'),(5,'201020468002@lfdc.edu.in','$2y$10$uPLusar8FoeTvvPhVgtg7eJgc.WD4uUD3kJazGbF7EwDrR6iVTqqm','2026-03-07 09:31:05',0,'2026-03-07 09:21:05'),(6,'201020468002@lfdc.edu.in','$2y$10$CugYm8E0NrQzHMak1rAP4.M8mWiGnQCiwTsByiPUcGuS453AS4nke','2026-03-07 09:32:07',0,'2026-03-07 09:22:07'),(8,'sudhaganisriganesh@gmail.com','$2y$10$wmzIzonSsrDGG42tgpgtbOXe5CStS0ONhmB9DC.OOvS7JKr0E2AHy','2026-03-11 14:29:28',0,'2026-03-11 14:19:28'),(9,'sudhaganisriganesh@gmail.com','$2y$10$FreuKicb45whiBUV.Lo2FuQyaswuCGYlDa5YHcqiDsSesbFG/GNw6','2026-03-11 14:30:58',0,'2026-03-11 14:20:58'),(10,'sudhaganisriganesh@gmail.com','$2y$10$isV9gI2KmFlYOx7gdUQ9gOVgHMt/ojN.3sAC7qaMVi.wnXPa/Ggly','2026-03-11 14:40:55',0,'2026-03-11 14:30:55');
/*!40000 ALTER TABLE `web_otp_verifications` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-20 13:32:44
