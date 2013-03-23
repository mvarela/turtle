(ns turtle.core)

;; Basic Turtle implementation
;; ---------------------------
;; Allows a seq of keywords to 'drive' a turtle on an HTML5 canvas:
;;
;;   (def commands '(:color :red, :fwd 50, :left 10, :fwd 45, :right 72)
;;   (draw! ctx commands [100 100])
;;
;; By default, the drawing is scaled to fill the screen dimensions
;; (in this case [100,100])

(def colors [:red :green :blue :yellow :cyan :magenta :orange :black "#663300" "#68FF33"])

(defn- bounding-box 
  "Calculates the smallest and largest [x,y] points"
  [coords]
  (let [[min-x max-x] (apply (juxt min max) (map first coords))
        [min-y max-y] (apply (juxt min max) (map second coords))]
   [[min-x min-y] [max-x max-y]]))

(def radians (/ Math/PI 180.0))

(defn- deg->rad [theta]
  (* theta radians))

(defn- move-forward 
  "Given a state (containing a heading), move forward by the supplied
   distance."
  [state dist]
  (let [rad   (deg->rad (:heading state))
        [x y] (:coords state)]
    (assoc state :coords [ (+ x (* dist (Math/cos rad))) 
                           (+ y (* dist (Math/sin rad)))])))

(defn- turn 
  "Given a state, and an operation (either the + or - function),
   update such that the new heading is altered by the angle"
  [op state angle]
  (let [heading (:heading state)]
    (assoc state :heading (mod (op heading angle) 360))))

(defn- update-color [state color]
  (assoc state :color color))

(defn- color-index [state index]
  (update-color state (get colors index)))

(defn- push-state [state _]
  (let [saved (select-keys state [:coords :heading])]
    (update-in state [:stack] #(conj % saved))))

(defn- pop-state [state _]
  (let [restored (peek (:stack state))]
    (-> state
      (merge restored {:restore-point true})
      (update-in [:stack] pop))))

(defn- goto-origin [state _]
  (merge state { :coords [0 0] :heading 90 :stack [] :restore-point true }))

(defn- pen-ops [state pen]
  (if (= pen :up)
    (assoc state :move true) ; :move is sticky, :restore-point is not
    (dissoc state :move)))

(def state-mapper
  { :color   update-color 
    :color-index color-index 
    :left    (partial turn +)
    :right   (partial turn -)
    :fwd     move-forward 
    :pen     pen-ops
    :save    push-state
    :restore pop-state 
    :origin  goto-origin}) 

(defn- next-state 
  "Evolves the current state and a given command to determine the next state,
   e.g. if the current position is (4,3) pointing north, then move to (4,4)
   and turn in to the heading relative to the command."
  ([] [])
  ([curr-state] curr-state)
  ([curr-state [cmd & peek-ahead]]
    (if-let [update-fn (get state-mapper cmd)]
      ; always need stack/heading/coords, but nothing else
      (update-fn (select-keys curr-state [:coords :heading :stack :move]) (first peek-ahead))
      curr-state)))

(defn- process [cmds]
  (let [init-state { :coords [0 0] :heading 90 :stack []}]
    (->>
      (flatten cmds)
      (partition-all 2 1)
      (filter #(state-mapper (first %)))
      (reductions next-state init-state)
      distinct)))

(defn- calc-matrix-transform 
  "Calculates an affine transform matrix which will scale a drawing 
   constrained by the min/max bounds to the given screen co-ords. Note
   that the drawing is flipped so (0,0) will be represented at (or near)
   the lower edge, not the upper edge."
  [[screen-x screen-y] [[min-x min-y] [max-x max-y]]]
  (let [scale-x (/ screen-x (- max-x min-x))
        scale-y (/ screen-y (- max-y min-y))
        scale   (min scale-x scale-y)]
    [ scale 0 0 (- scale) (* scale ( - min-x)) (* scale max-y) ])) 

(defn draw! [renderer screen-area cmds]
  (let [data   (process (concat [:color :red] cmds))
        bounds (bounding-box (map :coords data))
        matrix (calc-matrix-transform screen-area bounds)]
    (renderer data screen-area bounds matrix)))
