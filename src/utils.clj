(ns utils
  (:require [clojure.string :as str])
  (:require [clojure.java.io :as io])
  (:import java.util.Base64)
  (:require [clojure.edn :as edn]))

(defn load-config
  [filename]
  (edn/read-string (slurp filename)))

(defn to-int-array
  [arr]
  (map #(if (string? %) (Integer/parseInt %) %) arr))

(defn in?
  [coll elm]
  (some #(= elm %) coll))

(defn format-multiline
  [values]
  (str/join "\n" values))

(defn with-cache
  [dir name response]
  (let [subdir (subs name 0 3)]
    (let [filename (str dir "/" subdir "/" name)]
      (if (.exists (io/file filename))
        (slurp filename)
        (do
          (io/make-parents filename)
          (spit filename (response))
          (slurp filename))))))


(defn base64-encode
  [to-encode]
  (.encodeToString (Base64/getEncoder) (.getBytes to-encode)))