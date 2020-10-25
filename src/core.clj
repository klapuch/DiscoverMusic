(ns core
  (:require telegram)
  (:require utils)
  (:require last-fm)
  (:require music-brainz)
  (:require spotify)
  (:gen-class))

(defn- format-artist
  [{url :url name :name match :match}]
  (format "<a href=\"%s\">%s</a>: %s" url name match))

(defn- format-send-message
  [results term]
  (if (empty? results)
    (format "Sorry, there are no matches for \"%s\"." term)
    (format "There are your matches for \"%s\": \n\n%s" term (utils/format-multiline results))))

(defn- missing-artists
  [artist]
  (->> (last-fm/similar-artists artist)
       (filter music-brainz/missing-on-spotify?)
       (filter #(spotify/missing? (:name %)))
       (map format-artist)))

(defn- send-results!
  []
  (let [updates (telegram/get-updates!)]
    (let [telegram-new-ids (telegram/get-new-ids (telegram/get-ids updates) (telegram/get-stored-ids))]
      (telegram/store-ids! (telegram/get-ids updates))
      (->> (telegram/updates-by-id updates telegram-new-ids)
           (map telegram/send-message-get-request)
           (map #(telegram/send-message! (:chat-id %) (format-send-message (missing-artists (:text %)) (:text %))))))))

(print (send-results!))
