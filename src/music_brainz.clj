(ns music-brainz
  (:require [clj-http.client :as client])
  (:import [org.jsoup Jsoup])
  (:require utils))

(def ^{:private true, :const true} cache-dir "/home/dom/Projects/DiscoverMusic/cache")

(defn- get-body [url] (:body (client/get url)))
(defn- get-parsed-body [body] (Jsoup/parse body))

(defn- music-brainz-url
  ([] "https://musicbrainz.org")
  ([artist] (str (music-brainz-url) "/artist/" artist)))

(defn- fresh->on-spotify?
  [mbid]
  (not= 0 (.size (.select (.body (get-parsed-body (get-body (music-brainz-url mbid)))) ".spotify-favicon"))))

(defn- on-spotify?
  [mbid]
  (= "true" (utils/with-cache (str cache-dir "/music-brainz") mbid #(fresh->on-spotify? mbid))))

(defn- available?
  [artist]
  (contains? artist :mbid))

(defn missing-on-spotify?
  [artist]
  (and (available? artist) (not (on-spotify? (:mbid artist)))))
