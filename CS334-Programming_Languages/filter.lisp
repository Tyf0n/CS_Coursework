;Nathaniel Lim
;February 23, 2010
;CS 334 - Spring 2010


;Recursively call the function on the car of the list
;And only cons the car of the list to the filter of the 
;rest of the list if it tests true.
(defun filter (fn list)
  (cond ((eq list nil) nil)
	((funcall fn (car list)) (cons (car list) (filter fn (cdr list))))
	( t (filter fn (cdr list)))
  )
)

(defun even (x) (eq (mod x 2) 0))

;Set-intersect: nil is either list is nil.
;Otherwise: filter b with the test being a member of a
(defun set-intersect (a b)
  (cond ((eq a nil) nil)
	((eq b nil) nil)
	(t (filter #'(lambda(x) (member x a)) b))
  )
)

;Set-union: If one list is nil, the union is the other list
;Otherwise append a to the filter of b with the test being not a member of a
(defun set-union (a b)
  (cond ((eq a nil) b)
	((eq b nil) a)
	(t (append a (filter #'(lambda(x) (not(member x a))) b)))
  )
)

;Exists: returns whether the filter of a list in non-empty
(defun exists (fn list)
  (not (eq (filter fn list) nil))
)

;All: Basically checks every element to see if exists returns true
;There could be a more elegant way of doing this with exists.
;I had originally checked if the list-length of the list and the filtered list
;were the same.
(defun all (fn list)
  (cond ((eq list nil) nil)
	((eq (cdr list) nil) (exists fn list))
	(t (and (exists fn (cons (car list) nil)) (all fn (cdr list))))
  )
)


(filter #'even '(12 1 231 123 2 12 2 23 3 6))
(set-intersect '(1 2 3 4 5 6) '(3 4 5 6 7 8))
(set-union     '(1 2 3 4 5 6) '(3 4 5 6 7 8))
(exists #'(lambda (x) (eq x 2)) '(-1 0 1))
(exists #'(lambda (x) (eq x 2)) '(-1 0 1 2 3))
(all  #'(lambda (x) (eq x 2)) '(-1 0 1 2 3))
(all  #'(lambda (x) (eq x 2)) '(2 2 2 2 2 2 2))