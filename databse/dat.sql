-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: montfortug_erp_dev
-- ------------------------------------------------------
-- Server version	8.0.46-0ubuntu0.24.04.3

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
-- Dumping data for table `erp_academic_terms`
--

LOCK TABLES `erp_academic_terms` WRITE;
/*!40000 ALTER TABLE `erp_academic_terms` DISABLE KEYS */;
INSERT INTO `erp_academic_terms` VALUES (1,1,'TERM1','Term I','2026-02-02','2026-05-01',1,'CLOSED',0,'First Term of Academic Year 2026',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(2,1,'TERM2','Term II','2026-05-25','2026-08-21',2,'ACTIVE',1,'Second Term of Academic Year 2026',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(3,1,'TERM3','Term III','2026-09-14','2026-12-04',3,'PLANNED',0,'Third Term of Academic Year 2026',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47');
/*!40000 ALTER TABLE `erp_academic_terms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `erp_academic_years`
--

LOCK TABLES `erp_academic_years` WRITE;
/*!40000 ALTER TABLE `erp_academic_years` DISABLE KEYS */;
INSERT INTO `erp_academic_years` VALUES (1,'2026','Academic Year 2026','2026-02-02','2026-12-04',NULL,NULL,'ACTIVE',1,'Uganda Ministry of Education and Sports Academic Year 2026',1,0,1,'2026-07-29 10:51:46',1,'2026-07-29 10:51:46');
/*!40000 ALTER TABLE `erp_academic_years` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `erp_sections`
--

LOCK TABLES `erp_sections` WRITE;
/*!40000 ALTER TABLE `erp_sections` DISABLE KEYS */;
INSERT INTO `erp_sections` VALUES (1,1,1,10,'A','Section A',40,'Primary 7 - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(2,1,1,9,'A','Section A',40,'Primary 6 - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(3,1,1,8,'A','Section A',40,'Primary 5 - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(4,1,1,7,'A','Section A',40,'Primary 4 - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(5,1,1,6,'A','Section A',40,'Primary 3 - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(6,1,1,5,'A','Section A',40,'Primary 2 - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(7,1,1,4,'A','Section A',40,'Primary 1 - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(8,1,1,3,'A','Section A',40,'Top Class - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(9,1,1,2,'A','Section A',40,'Middle Class - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(10,1,1,1,'A','Section A',40,'Baby Class - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(11,2,1,10,'A','Section A',40,'Primary 7 - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(12,2,1,9,'A','Section A',40,'Primary 6 - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(13,2,1,8,'A','Section A',40,'Primary 5 - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(14,2,1,7,'A','Section A',40,'Primary 4 - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(15,2,1,6,'A','Section A',40,'Primary 3 - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(16,2,1,5,'A','Section A',40,'Primary 2 - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(17,2,1,4,'A','Section A',40,'Primary 1 - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(18,2,1,3,'A','Section A',40,'Top Class - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(19,2,1,2,'A','Section A',40,'Middle Class - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47'),(20,2,1,1,'A','Section A',40,'Baby Class - Section A - Academic Year 2026','ACTIVE',1,0,1,'2026-07-29 10:51:47',1,'2026-07-29 10:51:47');
/*!40000 ALTER TABLE `erp_sections` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-31  0:41:43
