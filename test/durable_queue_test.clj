(ns durable-queue-test
  (:require
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [durable-queue :refer :all]
    [criterium.core :as c]))

(defn clear-tmp-directory []
  (doseq [f (->> (#'durable-queue/directory->queue->slab-files "/tmp")
              vals
              (apply concat))]
    (.delete (io/file f))))

(deftest test-basic-put-take
  (clear-tmp-directory)
  (let [q (queues "/tmp" {:slab-size 1024})
        tasks (range 1e4)]
    (doseq [t tasks]
      (put! q :foo t))
    (is (= tasks (map deref (immediate-task-seq q :foo))))))

(deftest test-retry
  (clear-tmp-directory)
  (let [q (queues "/tmp")]

    (doseq [t (range 10)]
      (put! q :foo t))

    (let [tasks' (immediate-task-seq q :foo)]
      (is (= (range 10) (map deref tasks')))
      (doseq [t (take 5 tasks')]
        (complete! t))
      (doseq [t (range 10 15)]
        (put! q :foo t))))

  ;; create a new manager, which will mark all in-progress tasks as incomplete
  (let [q (queues "/tmp")
        tasks' (immediate-task-seq q :foo)]
    (is (= (range 5 15) (map deref tasks')))
    (doseq [t (take 5 tasks')]
      (complete! t)))
    
  (let [q (queues "/tmp")
        tasks' (immediate-task-seq q :foo)]
    (is (= (range 10 15) (map deref tasks')))
    (doseq [t (range 15 20)]
      (put! q :foo t)))

  (let [q (queues "/tmp" {:complete? even?})]
    (is (= (remove even? (range 10 20)) (map deref (immediate-task-seq q :foo))))))

;;;

(deftest ^:benchmark benchmark-put-take
  (clear-tmp-directory)

  (println "\n\n-- sync both")
  (let [q (queues "/tmp" {:fsync-put? true, :fsync-take? true})]
    (c/quick-bench
      (do
        (put! q :foo 1)
        (complete! (take! q :foo)))))

  (println "\n\n-- sync take")
  (let [q (queues "/tmp" {:fsync-put? false, :fsync-take? true})]
    (c/quick-bench
      (do
        (put! q :foo 1)
        (complete! (take! q :foo)))))

  (println "\n\n-- sync put")
  (let [q (queues "/tmp" {:fsync-put? true, :fsync-take? false})]
    (c/quick-bench
      (do
        (put! q :foo 1)
        (complete! (take! q :foo)))))

  (println "\n\n-- sync neither")
  (let [q (queues "/tmp" {:fsync-put? false, :fsync-take? false})]
    (c/quick-bench
      (do
        (put! q :foo 1)
        (complete! (take! q :foo))))))