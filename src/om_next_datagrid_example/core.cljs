(ns om-next-datagrid-example.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

(def init-data
  {:list [{:first "Ben" :last "Bitdiddle" :email "benb@example.com"}
          {:first "Alyssa" :last "Hacker" :email "aphacker@example.com"}
          {:first "Eva" :last "Ator" :email "eval@example.com"}
          {:first "Louis" :last "Reasoner" :email "prolog@example.com"}
          {:first "Cy" :last "Effect" :email "bugs@example.com"}
          {:first "Lem" :last "Tweakit" :email "morebugs@example.com"}
          {:first "John" :last "Wein" :email "wein@example.com"}
          {:first "Laura" :last "Mit" :email "aphacker@example.com"}
          {:first "Vicky" :last "Gold" :email "gold@example.com"}
          {:first "Louis" :last "Amstrong" :email "prolog@example.com"}
          {:first "Carol" :last "Corner" :email "corner@example.com"}
          {:first "Jason" :last "Smith" :email "smith@example.com"}]})

;; -----------------------------------------------------------------------------
;; Parsing

(defn get-people [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))


(defmulti read om/dispatch)

(defmethod read :list [{:keys [state] :as env} key params]
  (cljs.pprint/pprint key)
  (cljs.pprint/pprint state)
  {:value (get-people state key)})


;; -----------------------------------------------------------------------------
;; Components

(defui Person
       static om/Ident
       (ident [this {:keys [first]}]
              [:person/by-first first])
       static om/IQuery
       (query [this]
              '[:first :last :email])
       Object
       (render [this]
               ;(println "Render Person" (-> this om/props :first))
               (let [{:keys [first last email] :as props} (om/props this)]
                 (dom/li nil
                         (dom/label nil (str first ", " last " (" email ")" ))))))

(def person (om/factory Person {:keyfn :first}))

(defui ListView
       static om/IQuery
       (query [this]
              (let [subquery (om/get-query Person)]
                `[{:list ~subquery}]))
       Object
       (render [this]
               (println "Render ListView")
               (let [{:keys [list]} (om/props this)]
                 (apply dom/div nil
                        [(dom/h2 nil "Datagrid example")
                         (apply dom/ul nil
                                (map person list))]))))

(def reconciler
  (om/reconciler
    {:state  init-data
     :parser (om/parser {:read read :mutate nil})}))

(om/add-root! reconciler
              ListView (gdom/getElement "app"))
