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
-- Table structure for table `web_school_results`
--

DROP TABLE IF EXISTS `web_school_results`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `web_school_results` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `year` varchar(10) NOT NULL,
  `institution` varchar(255) NOT NULL,
  `train_male` int(11) DEFAULT 0,
  `train_female` int(11) DEFAULT 0,
  `course` varchar(255) DEFAULT NULL,
  `course_fr` varchar(255) DEFAULT NULL,
  `course_es` varchar(255) DEFAULT NULL,
  `course_it` varchar(255) DEFAULT NULL,
  `course_de` varchar(255) DEFAULT NULL,
  `certificate` varchar(100) DEFAULT NULL,
  `grad_male` int(11) DEFAULT 0,
  `grad_female` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `web_school_results`
--

LOCK TABLES `web_school_results` WRITE;
/*!40000 ALTER TABLE `web_school_results` DISABLE KEYS */;
INSERT INTO `web_school_results` VALUES (1,'2024','St. Kizito Nursery & Primary, Kyebando',41,39,'Nursery-Primary 7','Maternelle-Primaire 7','Infantil-Primaria 7','Materna-Primaria 7','Kindergarten-Grundschule 7','PLE',7,1,'2026-03-07 14:48:23'),(2,'2025','St. Kizito Nursery & Primary, Kyebando',79,99,'Nursery-Primary 7','Maternelle-Primaire 7','Infantil-Primaria 7','Materna-Primaria 7','Kindergarten-Grundschule 7','PLE',4,4,'2026-03-07 14:49:07'),(3,'2025','Kyaka II refugee camp- helped back to school',4,5,'Primary ','Primaire','Primaria','Primaria','Grundschule','PLE',0,0,'2026-03-07 14:49:57'),(4,'2026','St. Kizito Nursery & Primary, Kyebando',100,138,'Nursery-Primary 7','Maternelle-Primaire 7','Infantil-Primaria 7','Materna-Primaria 7','Kindergarten-Grundschule 7','PLE',0,0,'2026-03-07 14:50:49'),(5,'2026','St.Montfort Nursery & Primary School,Mpala',22,21,'Nursery-Primary 7','Maternelle-Primaire 7','Infantil-Primaria 7','Materna-Primaria 7','Kindergarten-Grundschule 7','PLE',0,0,'2026-03-07 14:51:27'),(6,'2026','Pere Achte Secondary School, Isunga',145,157,'S1-S4',NULL,NULL,NULL,NULL,'UCE',0,0,'2026-03-07 14:52:11'),(7,'2026','Pere Achte Senior Secondary School, Isunga',16,9,'S5-S6','S5-S6','S5-S6','S5-S6','S5-S6','UACE',0,0,'2026-03-07 14:53:00');
/*!40000 ALTER TABLE `web_school_results` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-20 13:32:35
