-- ============================================================
--  APEX F1 - full MySQL database (schema + full dataset)
--  Generated from the live database, matching the app's seed:
--  11 teams, 22 drivers (with headshots), 8 circuits (with track
--  maps), an 8-round calendar, championship standings, and the
--  Spanish GP session data (lap times / telemetry / weather).
--
--  This script DROPS and recreates the `apexf1` database, so it is
--  fully self-contained and re-runnable.
--
--  HOW TO RUN
--   MySQL Workbench: File > Open SQL Script > pick this file > run (lightning bolt)
--   Command line:    mysql -u root -p < apexf1.sql
--
--  NOTE: You normally don't need this - the app auto-creates the DB
--  and loads the same data on first run. Use this to build/inspect
--  the database manually or to submit the SQL separately.
-- ============================================================
--
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: apexf1
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `apexf1`
--

/*!40000 DROP DATABASE IF EXISTS `apexf1`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `apexf1` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `apexf1`;

--
-- Table structure for table `circuits`
--

DROP TABLE IF EXISTS `circuits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `circuits` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `city` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `country_code` varchar(255) DEFAULT NULL,
  `length_km` double DEFAULT NULL,
  `map_image_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `total_laps` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `circuits`
--

LOCK TABLES `circuits` WRITE;
/*!40000 ALTER TABLE `circuits` DISABLE KEYS */;
INSERT INTO `circuits` VALUES (1,'Sakhir','Bahrain','BH',5.412,'/img/tracks/bahrain.png','Bahrain International Circuit',57),(2,'Jeddah','Saudi Arabia','SA',6.174,'/img/tracks/jeddah.png','Jeddah Corniche Circuit',50),(3,'Suzuka','Japan','JP',5.807,'/img/tracks/suzuka.png','Suzuka International Racing Course',53),(4,'Monte Carlo','Monaco','MC',3.337,'/img/tracks/monaco.png','Circuit de Monaco',78),(5,'Barcelona','Spain','ES',4.657,'/img/tracks/barcelona.png','Circuit de Barcelona-Catalunya',66),(6,'Silverstone','United Kingdom','GB',5.891,'/img/tracks/silverstone.png','Silverstone Circuit',52),(7,'Spa','Belgium','BE',7.004,'/img/tracks/spa.png','Circuit de Spa-Francorchamps',44),(8,'Monza','Italy','IT',5.793,'/img/tracks/monza.png','Autodromo Nazionale Monza',53);
/*!40000 ALTER TABLE `circuits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `driver_standings`
--

DROP TABLE IF EXISTS `driver_standings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `driver_standings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `points` double DEFAULT NULL,
  `position` int DEFAULT NULL,
  `season` int NOT NULL,
  `wins` int DEFAULT NULL,
  `driver_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh0heleigwn53ie9i2go1rcql8` (`driver_id`),
  CONSTRAINT `FKh0heleigwn53ie9i2go1rcql8` FOREIGN KEY (`driver_id`) REFERENCES `drivers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `driver_standings`
--

LOCK TABLES `driver_standings` WRITE;
/*!40000 ALTER TABLE `driver_standings` DISABLE KEYS */;
INSERT INTO `driver_standings` VALUES (1,310,1,2026,8,7),(2,285,2,2026,6,8),(3,276,3,2026,5,5),(4,240,4,2026,2,1),(5,210,5,2026,1,4),(6,190,6,2026,0,3),(7,150,7,2026,0,2),(8,120,8,2026,0,14),(9,95,9,2026,0,13),(10,80,10,2026,0,11),(11,55,11,2026,0,17),(12,45,12,2026,0,15),(13,40,13,2026,0,6),(14,35,14,2026,0,16),(15,30,15,2026,0,19),(16,26,16,2026,0,9),(17,20,17,2026,0,18),(18,14,18,2026,0,12),(19,10,19,2026,0,20),(20,8,20,2026,0,10),(21,4,21,2026,0,21),(22,2,22,2026,0,22);
/*!40000 ALTER TABLE `driver_standings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drivers`
--

DROP TABLE IF EXISTS `drivers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `drivers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `car_number` int DEFAULT NULL,
  `code` varchar(3) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `headshot_url` varchar(255) DEFAULT NULL,
  `nationality` varchar(255) DEFAULT NULL,
  `team_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6pw1pdnuvpftd7wuvvxebrt9b` (`code`),
  KEY `FKcay8hmpefrncg4apmusc0mkth` (`team_id`),
  CONSTRAINT `FKcay8hmpefrncg4apmusc0mkth` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drivers`
--

LOCK TABLES `drivers` WRITE;
/*!40000 ALTER TABLE `drivers` DISABLE KEYS */;
INSERT INTO `drivers` VALUES (1,63,'RUS','George Russell','/img/drivers/rus.png','British',1),(2,12,'ANT','Andrea K. Antonelli','/img/drivers/ant.png','Italian',1),(3,44,'HAM','Lewis Hamilton','/img/drivers/ham.png','British',2),(4,16,'LEC','Charles Leclerc','/img/drivers/lec.png','Monegasque',2),(5,1,'VER','Max Verstappen','/img/drivers/ver.png','Dutch',3),(6,22,'TSU','Yuki Tsunoda','/img/drivers/tsu.png','Japanese',3),(7,4,'NOR','Lando Norris','/img/drivers/nor.png','British',4),(8,81,'PIA','Oscar Piastri','/img/drivers/pia.png','Australian',4),(9,18,'STR','Lance Stroll','/img/drivers/str.png','Canadian',5),(10,11,'PER','Sergio Perez','/img/drivers/per.png','Mexican',5),(11,10,'GAS','Pierre Gasly','/img/drivers/gas.png','French',6),(12,43,'COL','Franco Colapinto','/img/drivers/col.png','Argentine',6),(13,23,'ALB','Alexander Albon','/img/drivers/alb.png','Thai',7),(14,55,'SAI','Carlos Sainz','/img/drivers/sai.png','Spanish',7),(15,31,'OCO','Esteban Ocon','/img/drivers/oco.png','French',8),(16,87,'BEA','Oliver Bearman','/img/drivers/bea.png','British',8),(17,6,'HAD','Isack Hadjar','/img/drivers/had.png','French',9),(18,30,'LAW','Liam Lawson','/img/drivers/law.png','New Zealander',9),(19,27,'HUL','Nico Hulkenberg','/img/drivers/hul.png','German',10),(20,5,'BOR','Gabriel Bortoleto','/img/drivers/bor.png','Brazilian',10),(21,77,'BOT','Valtteri Bottas','/img/drivers/bot.png','Finnish',11),(22,24,'ZHO','Zhou Guanyu','/img/drivers/zho.png','Chinese',11);
/*!40000 ALTER TABLE `drivers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lap_times`
--

DROP TABLE IF EXISTS `lap_times`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lap_times` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `compound` enum('HARD','INTERMEDIATE','MEDIUM','SOFT','WET') DEFAULT NULL,
  `lap_number` int NOT NULL,
  `lap_time_ms` bigint DEFAULT NULL,
  `outlier` bit(1) NOT NULL,
  `pit_lap` bit(1) NOT NULL,
  `position` int DEFAULT NULL,
  `driver_id` bigint NOT NULL,
  `session_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8ahj8f9dmu1nabu0326haouyw` (`driver_id`),
  KEY `FKhupvw3g0id6fclj4gjnhqc5yq` (`session_id`),
  CONSTRAINT `FK8ahj8f9dmu1nabu0326haouyw` FOREIGN KEY (`driver_id`) REFERENCES `drivers` (`id`),
  CONSTRAINT `FKhupvw3g0id6fclj4gjnhqc5yq` FOREIGN KEY (`session_id`) REFERENCES `sessions` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lap_times`
--

LOCK TABLES `lap_times` WRITE;
/*!40000 ALTER TABLE `lap_times` DISABLE KEYS */;
INSERT INTO `lap_times` VALUES (1,'HARD',63,81200,_binary '\0',_binary '\0',1,1,2),(2,'HARD',64,80980,_binary '\0',_binary '\0',1,1,2),(3,'HARD',65,87127,_binary '\0',_binary '\0',1,1,2),(4,'MEDIUM',63,81410,_binary '\0',_binary '\0',2,7,2),(5,'MEDIUM',64,81250,_binary '\0',_binary '\0',2,7,2),(6,'MEDIUM',65,81190,_binary '\0',_binary '\0',2,7,2),(7,'HARD',63,81650,_binary '\0',_binary '\0',3,5,2),(8,'HARD',64,81420,_binary '\0',_binary '\0',3,5,2),(9,'HARD',65,81390,_binary '\0',_binary '\0',3,5,2);
/*!40000 ALTER TABLE `lap_times` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `predictions`
--

DROP TABLE IF EXISTS `predictions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `predictions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `accuracy_pct` double DEFAULT NULL,
  `fan_name` varchar(255) NOT NULL,
  `scored` bit(1) NOT NULL,
  `submitted_at` datetime(6) DEFAULT NULL,
  `predicted_pole_driver_id` bigint NOT NULL,
  `predicted_winner_driver_id` bigint NOT NULL,
  `race_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK15ewrve8n8oeto6016s3li655` (`predicted_pole_driver_id`),
  KEY `FKtbatuqehkdbauriwpuodyf9ua` (`predicted_winner_driver_id`),
  KEY `FK7q0a33bexj6n5fiqyjuh09jpi` (`race_id`),
  CONSTRAINT `FK15ewrve8n8oeto6016s3li655` FOREIGN KEY (`predicted_pole_driver_id`) REFERENCES `drivers` (`id`),
  CONSTRAINT `FK7q0a33bexj6n5fiqyjuh09jpi` FOREIGN KEY (`race_id`) REFERENCES `races` (`id`),
  CONSTRAINT `FKtbatuqehkdbauriwpuodyf9ua` FOREIGN KEY (`predicted_winner_driver_id`) REFERENCES `drivers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `predictions`
--

LOCK TABLES `predictions` WRITE;
/*!40000 ALTER TABLE `predictions` DISABLE KEYS */;
INSERT INTO `predictions` VALUES (2,100,'Piyush',_binary '','2026-09-22 15:29:23.632681',5,5,1),(3,50,'Akshay',_binary '','2026-09-22 15:29:23.779655',7,5,1),(4,0,'Sanjay Adithya',_binary '','2026-09-22 15:29:23.854446',1,7,1);
/*!40000 ALTER TABLE `predictions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `races`
--

DROP TABLE IF EXISTS `races`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `races` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `completed` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `race_date` date DEFAULT NULL,
  `round` int NOT NULL,
  `season` int NOT NULL,
  `actual_pole_driver_id` bigint DEFAULT NULL,
  `actual_winner_driver_id` bigint DEFAULT NULL,
  `circuit_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2eokvmxk59fxso15lu44fgmb7` (`actual_pole_driver_id`),
  KEY `FKf7hi5uw6kg5t5e4gey6cm1y3r` (`actual_winner_driver_id`),
  KEY `FKsmryk5wkwqwa2wukhuv7amoab` (`circuit_id`),
  CONSTRAINT `FK2eokvmxk59fxso15lu44fgmb7` FOREIGN KEY (`actual_pole_driver_id`) REFERENCES `drivers` (`id`),
  CONSTRAINT `FKf7hi5uw6kg5t5e4gey6cm1y3r` FOREIGN KEY (`actual_winner_driver_id`) REFERENCES `drivers` (`id`),
  CONSTRAINT `FKsmryk5wkwqwa2wukhuv7amoab` FOREIGN KEY (`circuit_id`) REFERENCES `circuits` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `races`
--

LOCK TABLES `races` WRITE;
/*!40000 ALTER TABLE `races` DISABLE KEYS */;
INSERT INTO `races` VALUES (1,_binary '','Bahrain GP','2026-03-08',1,2026,5,5,1),(2,_binary '','Saudi Arabian GP','2026-03-22',2,2026,7,8,2),(3,_binary '','Japanese GP','2026-04-05',3,2026,7,7,3),(4,_binary '','Monaco GP','2026-05-24',4,2026,4,7,4),(5,_binary '','Spanish GP','2026-06-07',5,2026,1,1,5),(6,_binary '\0','British GP','2026-07-05',6,2026,NULL,NULL,6),(7,_binary '\0','Belgian GP','2026-07-26',7,2026,NULL,NULL,7),(8,_binary '\0','Italian GP','2026-09-06',8,2026,NULL,NULL,8);
/*!40000 ALTER TABLE `races` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sessions`
--

DROP TABLE IF EXISTS `sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_start` datetime(6) DEFAULT NULL,
  `total_laps` int DEFAULT NULL,
  `type` enum('FP1','FP2','FP3','QUALIFYING','RACE') NOT NULL,
  `race_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKn0ic1rfgpokdge2d920swux54` (`race_id`),
  CONSTRAINT `FKn0ic1rfgpokdge2d920swux54` FOREIGN KEY (`race_id`) REFERENCES `races` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sessions`
--

LOCK TABLES `sessions` WRITE;
/*!40000 ALTER TABLE `sessions` DISABLE KEYS */;
INSERT INTO `sessions` VALUES (1,'2026-06-06 09:30:00.000000',20,'QUALIFYING',5),(2,'2026-06-07 09:30:00.000000',66,'RACE',5);
/*!40000 ALTER TABLE `sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team_standings`
--

DROP TABLE IF EXISTS `team_standings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_standings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `points` double DEFAULT NULL,
  `position` int DEFAULT NULL,
  `season` int NOT NULL,
  `wins` int DEFAULT NULL,
  `team_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKi9ij4wqsua0wrfpr91ff7snvi` (`team_id`),
  CONSTRAINT `FKi9ij4wqsua0wrfpr91ff7snvi` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team_standings`
--

LOCK TABLES `team_standings` WRITE;
/*!40000 ALTER TABLE `team_standings` DISABLE KEYS */;
INSERT INTO `team_standings` VALUES (1,595,1,2026,14,4),(2,400,2,2026,1,2),(3,390,3,2026,2,1),(4,316,4,2026,5,3),(5,215,5,2026,0,7),(6,94,6,2026,0,6),(7,80,7,2026,0,8),(8,75,8,2026,0,9),(9,40,9,2026,0,10),(10,34,10,2026,0,5),(11,6,11,2026,0,11);
/*!40000 ALTER TABLE `team_standings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teams`
--

DROP TABLE IF EXISTS `teams`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teams` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `color_hex` varchar(255) DEFAULT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `nationality` varchar(255) DEFAULT NULL,
  `short_name` varchar(3) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKa510no6sjwqcx153yd5sm4jrr` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teams`
--

LOCK TABLES `teams` WRITE;
/*!40000 ALTER TABLE `teams` DISABLE KEYS */;
INSERT INTO `teams` VALUES (1,'#00D2A0','/img/teams/mer.png','Mercedes','German','MER'),(2,'#E10600','/img/teams/fer.png','Ferrari','Italian','FER'),(3,'#1E5BC6','/img/teams/rbr.png','Red Bull Racing','Austrian','RBR'),(4,'#FF8000','/img/teams/mcl.png','McLaren','British','MCL'),(5,'#229971','/img/teams/ast.png','Aston Martin','British','AST'),(6,'#0093CC','/img/teams/alp.png','Alpine','French','ALP'),(7,'#64C4FF','/img/teams/wil.png','Williams','British','WIL'),(8,'#B6BABD','/img/teams/haas.png','Haas','American','HAS'),(9,'#6692FF','/img/teams/rb.png','Racing Bulls','Italian','RB'),(10,'#52E252','/img/teams/sau.png','Kick Sauber','Swiss','SAU'),(11,'#B8860B','/img/teams/cad.png','Cadillac','American','CAD');
/*!40000 ALTER TABLE `teams` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `telemetry_points`
--

DROP TABLE IF EXISTS `telemetry_points`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `telemetry_points` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `brake` double DEFAULT NULL,
  `distancem` double DEFAULT NULL,
  `gear` int DEFAULT NULL,
  `lap_number` int NOT NULL,
  `rpm` int DEFAULT NULL,
  `sample_index` int NOT NULL,
  `speed_kph` double DEFAULT NULL,
  `throttle_pct` double DEFAULT NULL,
  `driver_id` bigint NOT NULL,
  `session_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKno2q3lbkc3tavlwswmy5h3adt` (`driver_id`),
  KEY `FKcsbvc7dvphh5p7ho2jtdjkq5f` (`session_id`),
  CONSTRAINT `FKcsbvc7dvphh5p7ho2jtdjkq5f` FOREIGN KEY (`session_id`) REFERENCES `sessions` (`id`),
  CONSTRAINT `FKno2q3lbkc3tavlwswmy5h3adt` FOREIGN KEY (`driver_id`) REFERENCES `drivers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `telemetry_points`
--

LOCK TABLES `telemetry_points` WRITE;
/*!40000 ALTER TABLE `telemetry_points` DISABLE KEYS */;
INSERT INTO `telemetry_points` VALUES (1,0,0,8,43,11400,0,331,100,1,2),(2,0,460,7,43,11000,1,300,90,1,2),(3,0,920,6,43,10200,2,260,60,1,2),(4,1,1380,4,43,9000,3,210,20,1,2),(5,1,1840,3,43,8200,4,163,0,1,2),(6,0,2300,4,43,9200,5,175,15,1,2),(7,0,2760,5,43,10400,6,220,55,1,2),(8,0,3220,6,43,11000,7,270,85,1,2),(9,0,3680,7,43,11300,8,310,100,1,2),(10,0,4140,8,43,11400,9,331,100,1,2);
/*!40000 ALTER TABLE `telemetry_points` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `weather_samples`
--

DROP TABLE IF EXISTS `weather_samples`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `weather_samples` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `air_tempc` double DEFAULT NULL,
  `humidity_pct` double DEFAULT NULL,
  `pressure_mb` double DEFAULT NULL,
  `track_tempc` double DEFAULT NULL,
  `wind_speed_ms` double DEFAULT NULL,
  `session_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK42foyxmrcjqw40tf4m225pic0` (`session_id`),
  CONSTRAINT `FK42foyxmrcjqw40tf4m225pic0` FOREIGN KEY (`session_id`) REFERENCES `sessions` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `weather_samples`
--

LOCK TABLES `weather_samples` WRITE;
/*!40000 ALTER TABLE `weather_samples` DISABLE KEYS */;
INSERT INTO `weather_samples` VALUES (1,30.5,29,1001,50.3,2.6,2);
/*!40000 ALTER TABLE `weather_samples` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-22 21:04:46
