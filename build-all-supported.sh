#!/bin/bash

declare publish=false

main_versions=("1.21.11" "1.21.10" "1.21.8" "1.21.6" "1.21.5" "1.21.4" "1.21.3" "1.21.1" "1.20.6" "1.20.4" "1.20.2" "1.20.1")

if [ $# -eq 0 ]; then
  exit
else
  versions=( "$@" )
  if [[ $1 = "-h" || $1 = "-help" || $1 = "help" ]]; then
    printf "This script builds this project for multiple versions!\nYou can specify them in the command line seperated by spaces,\n or give no arguments to build for all versions.\n"
    exit
  elif [[ $1 = "all" ]]; then
    versions=("1.21.1" "1.21" "1.20.6" "1.20.5" "1.20.4" "1.20.3" "1.20.2" "1.20.1" "1.20")
  elif [[ $1 = "main" ]]; then
    versions=( "${main_versions[@]}" )
  elif [[ $1 = "publish" ]]; then
    unset 'versions[0]'
    source .env
    publish=true
    if [[ $2 = "main" ]]; then
      versions=( "${main_versions[@]}" )
    fi
  fi
fi

#versions=("1.21.1" "1.21" "1.20.6" "1.20.5" "1.20.4" "1.20.3" "1.20.2" "1.20.1" "1.20")
#versions=("1.20.4")
#versions=("1.20.1")

#declare -A jdk_21=(["1.21.1"]=1 ["1.21"]=1 ["1.20.6"]=1 ["1.20.5"]=1)
#declare -A jdk_17=(["1.20.4"]=1 ["1.20.3"]=1 ["1.20.2"]=1 ["1.20.1"]=1 ["1.20"]=1)

for version in "${versions[@]}"; do
  printf "\e[;32mBuilding %s\e[0m\n" "$version" # Building $version but GREEN
#  cmd.exe /c ".\gradlew.bat -Pminecraft_version=""$version"" -Dorg.gradle.java.home=C:\Progra~1\Java\jdk-21 help"
  cmd.exe /c ".\gradlew.bat -Pminecraft_version=""$version"" -Dorg.gradle.java.home=C:\Progra~1\Java\jdk-21 build"

  if [[ $publish = true ]]; then
    cmd.exe /c ".\gradlew.bat -Pminecraft_version=""$version"" -PMODRINTH_TOKEN=""$MODRINTH_API_KEY"" -Dorg.gradle.java.home=C:\Progra~1\Java\jdk-21 modrinth"
  fi


done