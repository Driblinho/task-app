-- MySQL dump 10.16  Distrib 10.1.14-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: migration
-- ------------------------------------------------------
-- Server version	10.1.14-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ApiKeys`
--

DROP TABLE IF EXISTS `ApiKeys`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ApiKeys` (
  `id_api` int(11) NOT NULL AUTO_INCREMENT,
  `nazwa_wartosci` varchar(126) NOT NULL,
  `wartosc` varchar(255) NOT NULL,
  PRIMARY KEY (`id_api`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ApiKeys`
--

LOCK TABLES `ApiKeys` WRITE;
/*!40000 ALTER TABLE `ApiKeys` DISABLE KEYS */;
/*!40000 ALTER TABLE `ApiKeys` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `HasloResetToken`
--

DROP TABLE IF EXISTS `HasloResetToken`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HasloResetToken` (
  `reset_id` int(11) NOT NULL AUTO_INCREMENT,
  `token` varchar(255) NOT NULL,
  `waznosc` datetime NOT NULL,
  `uzytkownik_id` int(11) NOT NULL,
  PRIMARY KEY (`reset_id`),
  KEY `idx_uzytkownik_id` (`uzytkownik_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `HasloResetToken`
--

LOCK TABLES `HasloResetToken` WRITE;
/*!40000 ALTER TABLE `HasloResetToken` DISABLE KEYS */;
/*!40000 ALTER TABLE `HasloResetToken` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Projekty`
--

DROP TABLE IF EXISTS `Projekty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Projekty` (
  `projekt_id` int(11) NOT NULL AUTO_INCREMENT,
  `nazwa` varchar(200) NOT NULL,
  `opis` text,
  `data_dodania` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `data_zakonczenia` datetime DEFAULT NULL,
  `lider` int(11) NOT NULL,
  `status` tinyint(1) NOT NULL,
  PRIMARY KEY (`projekt_id`),
  KEY `idx_lider` (`lider`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Projekty`
--

LOCK TABLES `Projekty` WRITE;
/*!40000 ALTER TABLE `Projekty` DISABLE KEYS */;
/*!40000 ALTER TABLE `Projekty` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ProjektyUzytkownicy`
--

DROP TABLE IF EXISTS `ProjektyUzytkownicy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProjektyUzytkownicy` (
  `projekt_uzytkownik` bigint(11) NOT NULL AUTO_INCREMENT,
  `uzytkownik_id` bigint(11) NOT NULL,
  `projekt_id` bigint(11) NOT NULL,
  `lider` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`projekt_uzytkownik`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ProjektyUzytkownicy`
--

LOCK TABLES `ProjektyUzytkownicy` WRITE;
/*!40000 ALTER TABLE `ProjektyUzytkownicy` DISABLE KEYS */;
/*!40000 ALTER TABLE `ProjektyUzytkownicy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Uzytkownicy`
--

DROP TABLE IF EXISTS `Uzytkownicy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Uzytkownicy` (
  `uzytkownik_id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `imie` varchar(255) NOT NULL,
  `nazwisko` varchar(255) NOT NULL,
  `haslo` char(60) NOT NULL,
  `typ` tinyint(1) DEFAULT NULL,
  `data_urodzenia` date NOT NULL,
  `telefon` varchar(16) NOT NULL,
  `avatar_id` varchar(255) DEFAULT NULL,
  `kod_pocztowy` varchar(10) NOT NULL,
  `miasto` varchar(50) NOT NULL,
  `ulica` varchar(255) NOT NULL,
  `aktywny` bit(1) NOT NULL DEFAULT b'0',
  `PESEL` char(11) NOT NULL,
  `nieudane_logowania` tinyint(1) NOT NULL DEFAULT '0',
  `blokada` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`uzytkownik_id`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  UNIQUE KEY `PESEL` (`PESEL`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Uzytkownicy`
--

LOCK TABLES `Uzytkownicy` WRITE;
/*!40000 ALTER TABLE `Uzytkownicy` DISABLE KEYS */;
/*!40000 ALTER TABLE `Uzytkownicy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Zadania`
--

DROP TABLE IF EXISTS `Zadania`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Zadania` (
  `zadanie_id` int(11) NOT NULL AUTO_INCREMENT,
  `nazwa` varchar(200) NOT NULL,
  `opis` text,
  `data_dodania` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `data_zakonczenia` date DEFAULT NULL,
  `stan` tinyint(1) NOT NULL,
  `projekt_id` int(11) NOT NULL,
  `uzytkownik_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`zadanie_id`) USING BTREE,
  KEY `idx_projekt_id` (`projekt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Zadania`
--

LOCK TABLES `Zadania` WRITE;
/*!40000 ALTER TABLE `Zadania` DISABLE KEYS */;
/*!40000 ALTER TABLE `Zadania` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ZadaniaKomentarze`
--

DROP TABLE IF EXISTS `ZadaniaKomentarze`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ZadaniaKomentarze` (
  `zadanie_komentarz_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `komentarz` text NOT NULL,
  `data_dodania` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `zadanie_id` bigint(20) NOT NULL,
  PRIMARY KEY (`zadanie_komentarz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ZadaniaKomentarze`
--

LOCK TABLES `ZadaniaKomentarze` WRITE;
/*!40000 ALTER TABLE `ZadaniaKomentarze` DISABLE KEYS */;
/*!40000 ALTER TABLE `ZadaniaKomentarze` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Zaproszenia`
--

DROP TABLE IF EXISTS `Zaproszenia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Zaproszenia` (
  `zaproszenie_id` int(11) NOT NULL AUTO_INCREMENT,
  `projekt_id` int(11) NOT NULL,
  `uzytkownik_id` int(11) NOT NULL,
  `komentarz` varchar(255) DEFAULT NULL,
  `stan` tinyint(1) DEFAULT NULL,
  `data_dodania` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`zaproszenie_id`),
  UNIQUE KEY `projekt_id` (`projekt_id`,`uzytkownik_id`) USING BTREE,
  KEY `idx_projekt_id` (`projekt_id`),
  KEY `idx_uzytkownik_id` (`uzytkownik_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Zaproszenia`
--

LOCK TABLES `Zaproszenia` WRITE;
/*!40000 ALTER TABLE `Zaproszenia` DISABLE KEYS */;
/*!40000 ALTER TABLE `Zaproszenia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'migration'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-06-01 18:48:37
