<?php
function autoriser_oss_onglet_dist($faire, $type, $id, $qui, $opt) {
	return ($qui['statut'] == '0minirezo');
}