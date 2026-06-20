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
-- Table structure for table `web_horizons`
--

DROP TABLE IF EXISTS `web_horizons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `web_horizons` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `title_fr` varchar(255) DEFAULT NULL,
  `title_es` varchar(255) DEFAULT NULL,
  `title_it` varchar(255) DEFAULT NULL,
  `title_de` varchar(255) DEFAULT NULL,
  `caption` text NOT NULL,
  `caption_fr` text DEFAULT NULL,
  `caption_es` text DEFAULT NULL,
  `caption_it` text DEFAULT NULL,
  `caption_de` text DEFAULT NULL,
  `label` varchar(50) DEFAULT 'Striving',
  `label_fr` varchar(50) DEFAULT NULL,
  `label_es` varchar(50) DEFAULT NULL,
  `label_it` varchar(50) DEFAULT NULL,
  `label_de` varchar(50) DEFAULT NULL,
  `image` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `is_active` tinyint(1) DEFAULT 1,
  `display_order` int(11) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `web_horizons`
--

LOCK TABLES `web_horizons` WRITE;
/*!40000 ALTER TABLE `web_horizons` DISABLE KEYS */;
INSERT INTO `web_horizons` VALUES (1,'Skill Development: Technical Education','Développement des compétences : Enseignement technique','Desarrollo de habilidades: Educación técnica','Sviluppo delle competenze: Istruzione tecnica','Kompetenzentwicklung: Technische Bildung','There is a lot of scope and need for technical education for better livelihood and for progress.','Il y a beaucoup de possibilités et de besoins en matière d\'enseignement technique pour de meilleurs moyens de subsistance et pour le progrès.','Hay un gran alcance y necesidad de educación técnica para un mejor sustento y progreso.','C\'è molto spazio e bisogno di istruzione tecnica per un migliore sostentamento e per il progresso.','Es gibt viel Spielraum und Bedarf an technischer Bildung für einen besseren Lebensunterhalt und für Fortschritt.','Striving','S\'efforcer','Esforzarse','Impegnarsi','Streben','skill_development__technical_education.webp','2026-03-07 14:54:50',1,1),(2,'Higher Education','Enseignement supérieur','Educación superior','Istruzione superiore','Höhere Bildung','Research and Development is essential at every level and the country has a lot of potential in awakening innovation.','La recherche et le développement sont essentiels à tous les niveaux et le pays a un grand potentiel pour éveiller l\'innovation.','La investigación y el desarrollo son esenciales en todos los niveles y el país tiene un gran potencial para despertar la innovación.','La ricerca e lo sviluppo sono essenziali a ogni livello e il paese ha un grande potenziale nel risvegliare l\'innovazione.','Forschung und Entwicklung sind auf jeder Ebene unerlässlich, und das Land hat ein großes Potenzial, Innovationen zu wecken.','Striving','S\'efforcer','Esforzarse','Impegnarsi','Streben','higher_education.webp','2026-03-07 14:55:25',1,2),(3,'Community Outreach: Empowerment','Sensibilisation communautaire : Autonomisation','Alcance comunitario: Empoderamiento','Coinvolgimento della comunità: Emancipazione','Gemeinschaftsarbeit: Ermächtigung','Children struggle to go to school for want of small amounts of money for stationery and other needs.\r\nWe helped a few back into the school with the help of the Sisters of Mary Reparartrix.','Les enfants ont du mal à aller à l\'école par manque de petites sommes d\'argent pour les fournitures scolaires et autres besoins.\nNous en avons aidé quelques-uns à retourner à l\'école avec l\'aide des Sœurs de Marie Réparatrice.','Los niños luchan por ir a la escuela por falta de pequeñas cantidades de dinero para útiles escolares y otras necesidades.\nAyudamos a algunos a regresar a la escuela con la ayuda de las Hermanas de María Reparadora.','I bambini faticano ad andare a scuola per mancanza di piccole somme di denaro per cancelleria e altre necessità.\nNe abbiamo aiutati alcuni a tornare a scuola con l\'aiuto delle Suore di Maria Riparatrice.','Kinder haben Schwierigkeiten, zur Schule zu gehen, weil ihnen kleine Geldbeträge für Schreibwaren und andere Bedürfnisse fehlen.\nWir haben einigen mit Hilfe der Schwestern von Maria Reparartrix geholfen, wieder in die Schule zu gehen.','','','','','','community_outreach__empowerment.webp','2026-03-07 14:55:54',1,3),(4,'Community Outreach: Enduring Hope','Sensibilisation communautaire : Espoir durable','Alcance comunitario: Esperanza perdurable','Coinvolgimento della comunità: Speranza duratura','Gemeinschaftsarbeit: Dauerhafte Hoffnung','Many struggle to find food and there is also a need for medical aid','Beaucoup ont du mal à trouver de la nourriture et il y a aussi un besoin d\'aide médicale.','Muchos luchan por encontrar comida y también hay necesidad de asistencia médica.','Molti faticano a trovare cibo e c\'è anche bisogno di assistenza medica.','Viele haben Mühe, Nahrung zu finden, und es besteht auch Bedarf an medizinischer Hilfe.','Striving','S\'efforcer','Esforzarse','Impegnarsi','Streben','community_outreach__enduring_hope.webp','2026-03-07 14:56:40',1,4);
/*!40000 ALTER TABLE `web_horizons` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-20 13:33:26
