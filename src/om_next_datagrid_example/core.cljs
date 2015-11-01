(ns om-next-datagrid-example.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-bootstrap.table :refer [table]]
            [om-bootstrap.pagination :as pg]
            [clojure.walk :refer [stringify-keys]]))

(enable-console-print!)

(def page-size 3)

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

(defn get-people [state key start end]
  (let [st @state]
    (-> (into [] (map #(get-in st %)) (get st key))
        (subvec start end))))


(defmulti read om/dispatch)

(defmethod read :list [{:keys [state] :as env} key {:keys [start end]}]
  {:value (get-people state key start end)})


;; -----------------------------------------------------------------------------
;; Components

(defui Person
       static om/Ident
       (ident [this {:keys [first]}]
              [:person/by-first first])
       static om/IQuery
       (query [this]
              '[:first :last :email]
              )
       Object
       (render [this]
               (let [{:keys [first last email] :as props} (om/props this)]
                 (dom/tr nil
                         [(dom/td nil first)
                          (dom/td nil last)
                          (dom/td nil email)]))))

(def person (om/factory Person {:keyfn :first}))

(defn get-headers [data]
  (keys (stringify-keys (first data))))

(defn page-previous [this]
  (om/set-params! this (-> (om/get-params this)
                           (update :start - page-size)
                           (update :end - page-size))))

(defn page-next [this]
  (om/set-params! this (-> (om/get-params this)
                           (update :start + page-size)
                           (update :end + page-size))))

(defui ListView
       static om/IQueryParams
       (params [this]
               {:start  0
                :end    page-size
                :person (om/get-query Person)})
       static om/IQuery
       (query [this]
              '[({:list ?person} {:start ?start :end ?end})])
       Object
       (render [this]
               (let [{:keys [list] :as props} (om/props this)]
                 (apply dom/div nil
                        [(dom/h2 nil "Datagrid example")
                         (table {:striped? true :bordered? true :condensed? true :hover? true}
                                (dom/thead nil
                                           (apply dom/tr nil
                                                  (map #(dom/th nil %) (get-headers list))))
                                (dom/tbody nil
                                           (map person list)))
                         (pg/pagination {}
                                        ; @todo
                                        (pg/previous {:on-click #(page-previous this)})
                                        (pg/page {:active? true} "1")
                                        (pg/page {} "2")
                                        (pg/page {} "3")
                                        (pg/next {:on-click #(page-next this)}))]))))

(def reconciler
  (om/reconciler
    {:state  init-data
     :parser (om/parser {:read read :mutate nil})}))

(om/add-root! reconciler
              ListView (gdom/getElement "app"))
