(ns spotify
  (:require [clj-http.client :as client])
  (:require [cheshire.core :as json])
  (:require [clojure.string :as str])
  (:require utils))

(def ^{:private true, :const true} config (utils/load-config "/home/dom/Projects/DiscoverMusic/config/config.local.edn"))
(def ^{:private true, :const true} client-id (:client-id (:spotify config)))
(def ^{:private true, :const true} client-secret (:client-secret (:spotify config)))

(defn- basic-auth-credentials
  [client-id client-secret]
  (utils/base64-encode (str client-id ":" client-secret)))

(defn- api-uri
  ([] "https://api.spotify.com/v1")
  ([path] (str (api-uri) "/" path))
  ([path query] (str (api-uri path) "?" query)))

(defn- accounts-uri
  ([] "https://accounts.spotify.com")
  ([path] (str (accounts-uri) "/" path)))

(defn- format-url-artist
  [artist]
  (str/replace artist #" " "+"))

(defn- search-uri
  [artist]
  (api-uri "search" (format "q=%s&type=artist&limit=1" (format-url-artist artist))))

(defn- access-token-response!
  [client-id client-secret]
  (client/post
    (accounts-uri "api/token")
    {:headers {:authorization (format "Basic %s" (basic-auth-credentials client-id client-secret))}
     :form-params {:grant_type "client_credentials"}}))

(defn- extracted-access-token!
  [client-id client-secret]
  (:access_token (json/parse-string (:body (access-token-response! client-id client-secret)) true)))

(defn- search-response!
  [artist token]
  (client/get
    (search-uri artist)
    {:headers {:authorization (format "Bearer %s" token)}}))

(def ^{:private true} search-response-memo (memoize search-response!))

(defn- searched-artists!
  [artist token]
  (map :name (:items (:artists (json/parse-string (:body (search-response-memo artist token)) true)))))

(def ^{:private true} extracted-access-token-memo (memoize extracted-access-token!))

(defn- format-artist
  [artist]
  ((comp str/lower-case str/trim) artist))

(defn missing?
  [artist]
  (let [artist (format-artist artist)]
    (empty?
      (->> (searched-artists! artist (extracted-access-token-memo client-id client-secret))
           (map format-artist)
           (drop-while #(not= artist %))))))
