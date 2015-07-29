#!/usr/bin/env bash
export ALPHABETDB=alphabet
export ALPHABETUSER=jetmbt


echo
echo "--------------------------------------------"
echo "This script will install PostgreSQL."
echo "and create alphabet database and jetmbt user."
echo "You may be prompted for sudo password."
echo "--------------------------------------------"
echo

read -e -p "Install PostgreSQL database? [y/n] " -i "n" installpg
if [ "$installpg" = "y" ]; then
  sudo apt-get install postgresql
  echo
  echo "You will now set the default password for the postgres user."
  echo "This will open a psql terminal, enter:"
  echo
  echo "\\password postgres"
  echo "password \"postgres\" assumed by default"
  echo
  echo "and follow instructions for setting postgres admin password."
  echo "Press Ctrl+D or type \\q to quit psql terminal"
  echo "START psql --------"
  sudo -u postgres psql postgres
  echo "END psql --------"
  echo
fi

read -e -p "Create Alphabet Database and user? [y/n] " -i "n" createdb
if [ "$createdb" = "y" ]; then
  echo "use password \"qwerty\" by default"
  echo
  sudo -u postgres createuser -D -A -P $ALPHABETUSER
  sudo -u postgres createdb -O $ALPHABETUSER $ALPHABETDB
  echo
fi

read -e -p "Invoke default table partition? [y/n] " -i "n" initdb

if [ "$initdb" = "y" ]; then
  sudo -u postgres -H -- psql -d $ALPHABETDB -a -f initDB.sql
  #что делать с ковычками?

  sudo -u postgres -H -- psql -U postgres -d $ALPHABETDB -c 'ALTER DATABASE alphabet OWNER TO jetmbt;'
  echo
fi

echo "Don't forget to add your settings to project preferences"