(ns telegram
  (:require [clj-http.client :as client])
  (:require [digest])
  (:require [cheshire.core :as json])
  (:require [clojure.string :as str])
  (:require utils))

(def ^{:private true, :const true} config (utils/load-config "/home/dom/Projects/DiscoverMusic/config/config.local.edn"))
(def ^{:private true, :const true} data-dir "/home/dom/Projects/DiscoverMusic/data")
(def ^{:private true, :const true} telegram-message-file (str data-dir "/telegram/messages/list.txt"))
(def ^{:private true, :const true} telegram-api-key (:key (:telegram config)))

(defn- telegram-uri
  ([] (format "https://api.telegram.org/bot%s" telegram-api-key))
  ([path] (str (telegram-uri) "/" path)))

(defn send-message!
  [id message]
  (client/post
    (telegram-uri "sendMessage")
    {:body (json/generate-string {:chat_id id :text message :parse_mode "html"})
     :content-type :json}))

(defn get-updates!
  []
  (:result (json/parse-string (:body (client/post (telegram-uri "getUpdates"))) true)))

(defn send-message-get-request
  [update]
  {:chat-id (:id (:from (:message update)))
   :text (:text (:message update))
   :id (:update_id update)})

(defn store-ids!
  [ids]
  (spit telegram-message-file (str/join "," ids)))

(defn get-stored-ids
  []
  (let [content (str/trim (slurp telegram-message-file))]
    (if (empty? content)
      []
      (str/split content #","))))

(defn get-ids
  [updates]
  (->> updates
       (map send-message-get-request)
       (map :id)))

(defn get-new-ids
  [telegram-ids file-ids]
  (clojure.set/difference
    (set (utils/to-int-array telegram-ids))
    (set (utils/to-int-array file-ids))))

(defn updates-by-id
  [updates ids]
  (filter #(utils/in? ids (:id (telegram/send-message-get-request %))) updates))
