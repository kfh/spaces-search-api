(ns user
  (:require [spaces-search-api.system :refer [spaces-system]] 
            [reloaded.repl :refer [system init start stop go reset]]))

(reloaded.repl/set-init! #(spaces-system {:web-host "127.0.0.1" :web-port 4444}))
