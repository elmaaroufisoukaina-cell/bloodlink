SET NAMES utf8mb4;

-- ---------- groupes sanguins et seuils d'alerte ----------
INSERT INTO groupe_sanguin (id, code, seuil_alerte) VALUES
(1,'A+',20),(2,'A-',8),(3,'B+',15),(4,'B-',5),
(5,'AB+',5),(6,'AB-',3),(7,'O+',20),(8,'O-',10);

-- ---------- compatibilites : (donneur, receveur), 27 lignes ----------
INSERT INTO compatibilite_groupe (groupe_donneur_id, groupe_receveur_id) VALUES
(8,1),(8,2),(8,3),(8,4),(8,5),(8,6),(8,7),(8,8),   -- O- donne a tous
(7,7),(7,1),(7,3),(7,5),                            -- O+
(2,2),(2,1),(2,6),(2,5),                            -- A-
(1,1),(1,5),                                        -- A+
(4,4),(4,3),(4,6),(4,5),                            -- B-
(3,3),(3,5),                                        -- B+
(6,6),(6,5),                                        -- AB-
(5,5);                                              -- AB+

-- ---------- parametres systeme ----------
INSERT INTO parametre_systeme (cle, valeur, description) VALUES
('AGE_MIN','18','Age minimum du donneur'),
('AGE_MAX','65','Age maximum du donneur'),
('POIDS_MIN_KG','50','Poids minimum du donneur en kg'),
('DELAI_DON_HOMME_JOURS','90','Delai minimum entre deux dons (homme), en jours'),
('DELAI_DON_FEMME_JOURS','120','Delai minimum entre deux dons (femme), en jours'),
('DUREE_GLOBULES_JOURS','42','Duree de conservation des globules rouges, en jours'),
('DUREE_PLAQUETTES_JOURS','5','Duree de conservation des plaquettes, en jours'),
('DUREE_PLASMA_JOURS','365','Duree de conservation du plasma, en jours');

-- ---------- hopitaux (le 4e n'est pas valide) ----------
INSERT INTO hopital (id, nom, adresse, ville, telephone, email, valide, heure_ouverture, heure_fermeture, duree_creneau_min, capacite_par_creneau) VALUES
(1,'CHU Ibn Rochd','Rue des Hopitaux','Casablanca','0522000001','contact@ibnrochd.ma',TRUE,'08:00:00','16:00:00',30,3),
(2,'Hopital Militaire Avicenne','Avenue des FAR','Rabat','0537000002','contact@avicenne.ma',TRUE,'09:00:00','17:00:00',20,2),
(3,'CHU Mohammed VI','Avenue Ibn Sina','Marrakech','0524000003','contact@chumarrakech.ma',TRUE,'08:30:00','15:30:00',30,4),
(4,'Clinique Al Amal','Boulevard Pasteur','Tanger','0539000004','contact@alamal.ma',FALSE,'09:00:00','13:00:00',30,2);

-- ---------- donneurs ----------
INSERT INTO donneur (id, cin, nom, prenom, date_naissance, sexe, poids_kg, telephone, ville, date_prochain_don, groupe_sanguin_id) VALUES
(1,'AB123456','Alami','Youssef','1995-04-12','HOMME',78.0,'0600000001','Casablanca',DATE_ADD(CURDATE(), INTERVAL 80 DAY),8),
(2,'CD234567','Bennani','Salma','1998-09-03','FEMME',60.0,'0600000002','Casablanca',DATE_ADD(CURDATE(), INTERVAL 105 DAY),7),
(3,'EE345678','Tazi','Omar','1990-01-20','HOMME',85.0,'0600000003','Rabat',DATE_ADD(CURDATE(), INTERVAL 78 DAY),1),
(4,'FA456789','Idrissi','Khadija','1992-06-30','FEMME',58.0,'0600000004','Rabat',DATE_ADD(CURDATE(), INTERVAL 112 DAY),2),
(5,'GB567890','Chraibi','Mehdi','1988-11-11','HOMME',90.0,'0600000005','Marrakech',DATE_ADD(CURDATE(), INTERVAL 85 DAY),3),
(6,'HC678901','Lahlou','Imane','2000-02-14','FEMME',55.0,'0600000006','Casablanca',NULL,5),
(7,'JD789012','Berrada','Anas','1985-07-07','HOMME',72.0,'0600000007','Casablanca',DATE_ADD(CURDATE(), INTERVAL 52 DAY),8),
(8,'KE890123','Fassi','Nada','1996-12-25','FEMME',62.0,'0600000008','Marrakech',DATE_ADD(CURDATE(), INTERVAL 90 DAY),4),
(9,'LF901234','Sekkat','Rachid','1993-03-03','HOMME',80.0,'0600000009','Casablanca',NULL,8),
(10,'MG012345','El Idrissi','Hind','1997-08-19','FEMME',64.0,'0600000010','Casablanca',NULL,8),
(11,'NH123450','Ouazzani','Karim','1991-05-05','HOMME',76.0,'0600000011','Rabat',NULL,7);

-- ---------- utilisateurs (mot de passe : Bloodlink123) ----------
INSERT INTO utilisateur (id, email, mot_de_passe, role, actif, donneur_id, hopital_id) VALUES
(1,'admin@bloodlink.ma','$2a$10$CIYeaBMJUBJ6PFjHVFFGFuLncqzuyeGWhS386R/fvpp/Lhe3lInF.','ADMIN_SYSTEME',TRUE,NULL,NULL),
(2,'admin.casa@bloodlink.ma','$2a$10$CIYeaBMJUBJ6PFjHVFFGFuLncqzuyeGWhS386R/fvpp/Lhe3lInF.','ADMIN_HOPITAL',TRUE,NULL,1),
(3,'medecin.casa@bloodlink.ma','$2a$10$CIYeaBMJUBJ6PFjHVFFGFuLncqzuyeGWhS386R/fvpp/Lhe3lInF.','MEDECIN',TRUE,NULL,1),
(4,'stock.casa@bloodlink.ma','$2a$10$CIYeaBMJUBJ6PFjHVFFGFuLncqzuyeGWhS386R/fvpp/Lhe3lInF.','GESTIONNAIRE_STOCK',TRUE,NULL,1),
(5,'admin.rabat@bloodlink.ma','$2a$10$CIYeaBMJUBJ6PFjHVFFGFuLncqzuyeGWhS386R/fvpp/Lhe3lInF.','ADMIN_HOPITAL',TRUE,NULL,2),
(6,'medecin.rabat@bloodlink.ma','$2a$10$CIYeaBMJUBJ6PFjHVFFGFuLncqzuyeGWhS386R/fvpp/Lhe3lInF.','MEDECIN',TRUE,NULL,2),
(7,'stock.rabat@bloodlink.ma','$2a$10$CIYeaBMJUBJ6PFjHVFFGFuLncqzuyeGWhS386R/fvpp/Lhe3lInF.','GESTIONNAIRE_STOCK',TRUE,NULL,2),
(8,'admin.marrakech@bloodlink.ma','$2a$10$CIYeaBMJUBJ6PFjHVFFGFuLncqzuyeGWhS386R/fvpp/Lhe3lInF.','ADMIN_HOPITAL',TRUE,NULL,3),
(9,'medecin.marrakech@bloodlink.ma','$2a$10$CIYeaBMJUBJ6PFjHVFFGFuLncqzuyeGWhS386R/fvpp/Lhe3lInF.','MEDECIN',TRUE,NULL,3),
(10,'stock.marrakech@bloodlink.ma','$2a$10$CIYeaBMJUBJ6PFjHVFFGFuLncqzuyeGWhS386R/fvpp/Lhe3lInF.','GESTIONNAIRE_STOCK',TRUE,NULL,3),
(11,'youssef.alami@mail.ma','$2a$10$CIYeaBMJUBJ6PFjHVFFGFuLncqzuyeGWhS386R/fvpp/Lhe3lInF.','DONNEUR',TRUE,1,NULL),
(12,'rachid.sekkat@mail.ma','$2a$10$CIYeaBMJUBJ6PFjHVFFGFuLncqzuyeGWhS386R/fvpp/Lhe3lInF.','DONNEUR',TRUE,9,NULL),
(13,'salma.bennani@mail.ma','$2a$10$CIYeaBMJUBJ6PFjHVFFGFuLncqzuyeGWhS386R/fvpp/Lhe3lInF.','DONNEUR',TRUE,2,NULL);

-- ---------- dons (le don 8 est refuse) ----------
INSERT INTO don (id, date_don, type_don, volume_ml, statut, donneur_id, hopital_id, medecin_id) VALUES
(1,DATE_SUB(CURDATE(), INTERVAL 10 DAY),'SANG_TOTAL',450,'REALISE',1,1,3),
(2,DATE_SUB(CURDATE(), INTERVAL 38 DAY),'SANG_TOTAL',450,'REALISE',7,1,3),
(3,DATE_SUB(CURDATE(), INTERVAL 15 DAY),'SANG_TOTAL',450,'REALISE',2,1,3),
(4,DATE_SUB(CURDATE(), INTERVAL 12 DAY),'SANG_TOTAL',450,'REALISE',3,2,6),
(5,DATE_SUB(CURDATE(), INTERVAL 8 DAY),'SANG_TOTAL',450,'REALISE',4,2,6),
(6,DATE_SUB(CURDATE(), INTERVAL 5 DAY),'SANG_TOTAL',450,'REALISE',5,3,9),
(7,DATE_SUB(CURDATE(), INTERVAL 30 DAY),'SANG_TOTAL',450,'REALISE',8,3,9),
(8,DATE_SUB(CURDATE(), INTERVAL 3 DAY),'SANG_TOTAL',450,'REFUSE',6,1,3);

INSERT INTO questionnaire_eligibilite (poids_kg, hemoglobine, tension_systolique, voyage_recent, tatouage_recent, sous_traitement, eligible, motif_refus, don_id) VALUES
(78.0,14.5,120,FALSE,FALSE,FALSE,TRUE,NULL,1),
(72.0,14.0,125,FALSE,FALSE,FALSE,TRUE,NULL,2),
(60.0,13.0,115,FALSE,FALSE,FALSE,TRUE,NULL,3),
(85.0,15.0,130,FALSE,FALSE,FALSE,TRUE,NULL,4),
(58.0,12.8,110,FALSE,FALSE,FALSE,TRUE,NULL,5),
(90.0,15.2,128,FALSE,FALSE,FALSE,TRUE,NULL,6),
(62.0,13.1,118,FALSE,FALSE,FALSE,TRUE,NULL,7),
(55.0,11.2,105,FALSE,FALSE,FALSE,FALSE,'Hemoglobine trop basse (11.2 g/dl)',8);

-- ---------- demandes entre hopitaux et leurs lignes ----------
INSERT INTO demande_sang (id, date_demande, statut, commentaire, hopital_demandeur_id, hopital_fournisseur_id, utilisateur_id) VALUES
(1,DATE_SUB(NOW(), INTERVAL 1 DAY),'EN_ATTENTE','Besoin urgent au bloc',2,1,5),
(2,DATE_SUB(NOW(), INTERVAL 6 DAY),'SERVIE',NULL,3,1,8),
(3,DATE_SUB(NOW(), INTERVAL 4 DAY),'REFUSEE','Stock insuffisant',1,3,2),
(4,DATE_SUB(NOW(), INTERVAL 2 DAY),'PARTIELLEMENT_SERVIE',NULL,3,2,8);

INSERT INTO ligne_demande (id, type_produit, quantite, urgence, groupe_sanguin_id, demande_id) VALUES
(1,'GLOBULES_ROUGES',2,'URGENTE',1,1),
(2,'PLASMA',1,'NORMALE',7,1),
(3,'GLOBULES_ROUGES',1,'CRITIQUE',8,2),
(4,'PLAQUETTES',1,'URGENTE',3,3),
(5,'GLOBULES_ROUGES',2,'URGENTE',1,4);

-- ---------- poches (la poche 3 est perimee mais encore DISPONIBLE : test de la tache quotidienne) ----------
INSERT INTO poche_sanguine (id, numero_serie, type_produit, volume_ml, date_prelevement, date_peremption, statut, don_id, hopital_id, ligne_demande_id) VALUES
(1,'PO-0001','GLOBULES_ROUGES',280,DATE_SUB(CURDATE(), INTERVAL 10 DAY),DATE_ADD(CURDATE(), INTERVAL 32 DAY),'DISPONIBLE',1,1,NULL),
(2,'PO-0002','PLASMA',250,DATE_SUB(CURDATE(), INTERVAL 10 DAY),DATE_ADD(CURDATE(), INTERVAL 355 DAY),'DISPONIBLE',1,1,NULL),
(3,'PO-0003','PLAQUETTES',200,DATE_SUB(CURDATE(), INTERVAL 10 DAY),DATE_SUB(CURDATE(), INTERVAL 5 DAY),'DISPONIBLE',1,1,NULL),
(4,'PO-0004','GLOBULES_ROUGES',280,DATE_SUB(CURDATE(), INTERVAL 38 DAY),DATE_ADD(CURDATE(), INTERVAL 4 DAY),'DISPONIBLE',2,1,NULL),
(5,'PO-0005','GLOBULES_ROUGES',280,DATE_SUB(CURDATE(), INTERVAL 15 DAY),DATE_ADD(CURDATE(), INTERVAL 27 DAY),'DISPONIBLE',3,1,NULL),
(6,'PO-0006','PLASMA',250,DATE_SUB(CURDATE(), INTERVAL 15 DAY),DATE_ADD(CURDATE(), INTERVAL 350 DAY),'DISPONIBLE',3,1,NULL),
(7,'PO-0007','GLOBULES_ROUGES',280,DATE_SUB(CURDATE(), INTERVAL 12 DAY),DATE_ADD(CURDATE(), INTERVAL 30 DAY),'DISPONIBLE',4,2,NULL),
(8,'PO-0008','GLOBULES_ROUGES',280,DATE_SUB(CURDATE(), INTERVAL 8 DAY),DATE_ADD(CURDATE(), INTERVAL 34 DAY),'DISPONIBLE',5,2,NULL),
(9,'PO-0009','PLASMA',250,DATE_SUB(CURDATE(), INTERVAL 8 DAY),DATE_ADD(CURDATE(), INTERVAL 357 DAY),'DISPONIBLE',5,2,NULL),
(10,'PO-0010','GLOBULES_ROUGES',280,DATE_SUB(CURDATE(), INTERVAL 5 DAY),DATE_ADD(CURDATE(), INTERVAL 37 DAY),'DISPONIBLE',6,3,NULL),
(11,'PO-0011','GLOBULES_ROUGES',280,DATE_SUB(CURDATE(), INTERVAL 30 DAY),DATE_ADD(CURDATE(), INTERVAL 12 DAY),'DISPONIBLE',7,3,NULL),
(12,'PO-0012','PLASMA',250,DATE_SUB(CURDATE(), INTERVAL 30 DAY),DATE_ADD(CURDATE(), INTERVAL 335 DAY),'DISPONIBLE',7,3,NULL),
(13,'PO-0013','GLOBULES_ROUGES',280,DATE_SUB(CURDATE(), INTERVAL 10 DAY),DATE_ADD(CURDATE(), INTERVAL 32 DAY),'SERVIE',1,1,3),
(14,'PO-0014','GLOBULES_ROUGES',280,DATE_SUB(CURDATE(), INTERVAL 12 DAY),DATE_ADD(CURDATE(), INTERVAL 30 DAY),'SERVIE',4,2,5);

-- ---------- rendez-vous ----------
INSERT INTO rendez_vous (id, date_heure, statut, donneur_id, hopital_id) VALUES
(1,TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY),'09:00:00'),'PLANIFIE',9,1),
(2,TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY),'09:00:00'),'PLANIFIE',10,1),
(3,TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY),'10:00:00'),'PLANIFIE',11,2),
(4,TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY),'08:30:00'),'HONORE',6,1),
(5,TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 10 DAY),'09:00:00'),'HONORE',1,1),
(6,TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 20 DAY),'10:00:00'),'ANNULE',2,1);
