<?php
$GLOBALS[$GLOBALS['idx_lang']] = array(
	'gestion_indexation' => "Gestion de l'indexation",
	'titre_page' => "Configuration de l'indexation OSS",
	'titre_cfg_indexation' => "Configuration des indexations",
	'titre_cfg_moteur' => "Configuration du Moteur",
	'titre_cfg_champs' => "Affectation des champs",
	'titre_indexation_article' => "Indexation des articles",

	'last_indexed_document' => 'Dernier document indéxé',
	'last_indexation_date' => 'Date de la dernière indexation',
	'index_size' => 'Taille de l\'indexe',
	'status_writer' => 'Status de l\'indexateur',
	'engine_url' => 'URL du moteur',
	'engine_index' => 'Nom de l\'indexe',
	'indexed_document_count' => 'Nombre de documents indéxés',
	'engine_version' => 'Version du moteur',
	'url_interface' => 'URL d\'accès à l\'interface',
	'user' => 'Utilisateur',
	'password' => 'Mot de passe',
	'index' => 'Index',
	'engine' => 'Moteur',

	'text_server_unavailable' => "Aucun serveur ne répond à l'adresse fournie. Vérifiez l'URL du moteur.",
	'text_engine_unavailable' => "Le serveur répond, mais aucun service OpenSearchServer n'a été trouvé. Vérifiez que l'url du serveur est correcte.<br/><br/>Si l'url est correcte, il se peut que l'administrateur du serveur Tomcat ait désactivé OpenSearchServer.",
	'text_index_unavailable'  => "L'index configuré pour l'indexation semble ne pas exister. Vérifiez que vous avez configuré correctement le nom de l'index ainsi que les éventuels paramètre d'authentification.<br/><br>Si tout les paramètres sont correcte, contactez la personne en charge de votre service OpenSearchServer.",
	'engine_unavailabe' => "Moteur indisponible",


	'oss_indexation_global_enabled' => 'Activer l\'indexation',

	'oss_indexation_global_will_be_disabled' => 'Attention, vous allez désactiver l\'indexation de contenu.<br/>Plus rien ne sera envoyé au moteur. Aucune suppression ne sera effectué, aucune mise à jour ou nouvel ajout.<br/><br/>Il est de votre responssabilité de relancer une indexation par la suite pour prendre en compte les dernière modifications.',
	'oss_indexation_global_is_disabled' => 'L\'indexation du contenu est entièrement désactivée.',

	'indexation_article' => 'Indexation des articles',
	'indexation_breve' => 'Indexation des brèves',
	'indexation_rubrique' => 'Indexation des rubriques',

	'info_configuration_indexation' => 'Vous pouvez sur cette page:<ul style="padding-left:16px">
										<li>Activer et désactiver l\'indexation</li>
										<li>Les types de données qui seront indéxer dans le moteur de recherche</li>
										<li>Pour chaque types de données ce que vous voulez indéxer</li>
										</ul>',

	'oss_indexation_article_enabled' => 'Indexer les articles ?',
	'oss_indexation_article_surtitre' => 'Indexer le surtitre',
	'oss_indexation_article_titre' => 'Indexer le titre',
	'oss_indexation_article_soustitre' => 'Indexer le sous titre',
	'oss_indexation_article_nom_rubrique' => 'Indexer le nom de rubrique',
	'oss_indexation_article_descriptif' => 'Indexer la description',
	'oss_indexation_article_chapo' => 'Indexer le chapo',
	'oss_indexation_article_date' => 'Indexer la date',
	'oss_indexation_article_statut' => 'Indexer le status',
	'oss_indexation_article_maj' => 'Indexer la date de mise à jour',
	'oss_indexation_article_date_redac' => 'Indexer la date de rédaction',
	'oss_indexation_article_date_modif' => 'Indexer la date de modification',
	'oss_indexation_article_langue_choisie' => 'Indexer la langue choisie',
	'oss_indexation_article_lang' => 'Indexer la langue',
	'oss_indexation_article_texte' => 'Indexer le contenu',
	'oss_indexation_article_nom_auteur' => 'Indexer l\'auteur',

	'oss_indexation_breve_enabled' => 'Indexer les brêves ?',
	'oss_indexation_breve_date_heure' => 'Indexer la date',
	'oss_indexation_breve_titre' => 'Indexer le titre',
	'oss_indexation_breve_texte' => 'Indexer le contenu',
	'oss_indexation_breve_nom_rubrique' => 'Indexer le nom de rubrique',
	'oss_indexation_breve_lang' => 'Indexer la langue',
	'oss_indexation_breve_langue_choisie' => 'Indexer la langue choisie',
	'oss_indexation_breve_maj' => 'Indexer la date de mise à jour',

	'oss_indexation_rubrique_enabled' => 'Indexer les rubriques ?',
	'oss_indexation_rubrique_titre' => 'Indexer le titre',
	'oss_indexation_rubrique_descriptif' => 'Indexer la description',
	'oss_indexation_rubrique_texte' => 'Indexer le contenu',
	'oss_indexation_rubrique_maj' => 'Indexer la date de mise à jour',
	'oss_indexation_rubrique_date' => 'Indexer la date',
	'oss_indexation_rubrique_lang' => 'Indexer la langue',
	'oss_indexation_rubrique_langue_choisie' => 'Indexer la langue choisie',
);