(ns last-fm
  (:require [clj-http.client :as client])
  (:require [clojure.string :as str])
  (:require digest)
  (:require [cheshire.core :as json])
  (:require utils))

(def ^{:private true, :const true} config (utils/load-config "/home/dom/Projects/DiscoverMusic/config/config.local.edn"))
(def ^{:private true, :const true} cache-dir "/home/dom/Projects/DiscoverMusic/cache")
(def ^{:private true, :const true} api-key (:key (:last-fm config)))

(defn- similar-url
  [artist last-fm-api-key limit]
  (format "https://ws.audioscrobbler.com/2.0/?method=artist.getsimilar&artist=%s&api_key=%s&format=json&limit=%d" artist last-fm-api-key limit))

(defn- fresh->similar-artists
  [artist]
  (let [limit 400]
    (:body (client/get (similar-url artist api-key limit)))))

(defn- cached->similar-artists
  [artist]
  (let [artist (str/trim artist)]
    (utils/with-cache (str cache-dir "/last.fm") (digest/md5 artist) #(fresh->similar-artists artist))))

(defn- parsed-artists
  [payload]
  (:artist (:similarartists (json/parse-string payload true))))

(defn- relevant?
  ([artist relevance] (>= (Float/parseFloat (:match artist)) relevance))
  ([artist] (relevant? artist 0.5)))

(defn similar-artists
  [artist]
  (take-while relevant? (parsed-artists (cached->similar-artists artist))))
