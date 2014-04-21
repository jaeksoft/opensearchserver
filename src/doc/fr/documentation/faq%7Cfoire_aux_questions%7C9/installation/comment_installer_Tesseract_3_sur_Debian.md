Par défaut Debian fourni seulement la version 2.x de Tesseract. OpenSearchServer nécessite la version Tesseract 3. Voici comment installer Tesseract 3.x en utilisant les sources.

**Installez les outils de copmilation: g++ and make**

    root@ssd2:~# apt-get install g++
    root@ssd2:~# apt-get install make
  
**Télécharger, compilez et installez Leptonica**

[http://www.leptonica.com/download.html](http://www.leptonica.com/download.html)

Nous utilisons une installation standard dans `/usr/local`.


    root@ssd2:~# wget http://www.leptonica.com/source/leptonica-1.69.tar.gz
    root@ssd2:~# gunzip leptonica-1.69.tar.gz
    root@ssd2:~# tar -xvf leptonica-1.69.tar
    root@ssd2:~# cd leptonica-1.69
    root@ssd2:~/leptonica-1.69# ./configure
    root@ssd2:~/leptonica-1.69# make
    root@ssd2:~/leptonica-1.69# make install

**Téléchargez, compilez et installez Tesseract 3.x**

[http://code.google.com/p/tesseract-ocr/](http://code.google.com/p/tesseract-ocr/)

Nous utilisons une installation standard dans `/usr/local`.

    root@ssd2:~# wget http://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.02.tar.gz
    root@ssd2:~# gunzip tesseract-ocr-3.02.02.tar.gz
    root@ssd2:~# tar -xvf tesseract-ocr-3.02.02.tar
    root@ssd2:~# cd tesseract-ocr
    root@ssd2:~/tesseract-ocr# ./configure
    root@ssd2:~/tesseract-ocr# make
    root@ssd2:~/tesseract-ocr# make install

**Téléchargez et installez les langues nécessaires**

    root@ssd2:~# wget http://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.eng.tar.gz
    root@ssd2:~# wget http://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.spa.tar.gz
    root@ssd2:~# wget http://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.ita.tar.gz
    root@ssd2:~# wget http://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.deu.tar.gz
    root@ssd2:~# wget http://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.fra.tar.gz
    root@ssd2:~# gunzip tesseract-ocr-3.02.???.tar.gz
    root@ssd2:~# tar -xvf tesseract-ocr-3.02.eng.tar
    root@ssd2:~# tar -xvf tesseract-ocr-3.02.spa.tar
    root@ssd2:~# tar -xvf tesseract-ocr-3.02.ita.tar
    root@ssd2:~# tar -xvf tesseract-ocr-3.02.deu.tar
    root@ssd2:~# tar -xvf tesseract-ocr-3.02.fra.tar
    root@ssd2:~# cd tesseract-ocr/tessdata/
    root@ssd2:~/tesseract-ocr/tessdata# cp *.traineddata /usr/local/share/tessdata/


**Configurez les valeurs appropriées pour PATH et LD_LIBRARY_PATH**

    root@ssd2:~/tesseract-ocr/tessdata# export PATH=$PATH:/usr/local/bin
    root@ssd2:~/tesseract-ocr/tessdata# export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib
    root@ssd2:~/tesseract-ocr/tessdata# tesseract --list-langs
    List of available languages (10):
    dan-frak
    deu-frak
    deu
    eng
    fra
    ita
    ita_old
    slk-frak
    spa
    spa_old

La définition de ces variables d'environnement peut être intégrée dans le script de démarrage d'OpenSearchServer.

**Redémarrez OpenSearchServer et configurer l'onglet Runtime/Advanced**