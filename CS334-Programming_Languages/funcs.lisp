;Nathaniel Lim
;Feb 15, 2010
;CS334 - Williams College
;HW 1

; Using mapcar
'Functions
(defun double (list)
  (mapcar #'(lambda (x) (* 2 x)) list)
)

;Using recursion
(defun double2 (list)
  (cond ((eq nil list) nil)
	(t (cons (* 2 (car list)) (double2 (cdr list))))
  )
)

(defun list-length(x)
  (cond ((eq l nil) 1)
	(t (+ 1 (list-length(cdr x))))
  )
)

(defun square(x)
  (* x x)
)

(defun power-eff(b e)
  (cond ((eq e 0) 1)
	((eq (mod e 2) 0) (square (power-eff b (/ e 2))))
	( t (* b (power-eff b (- e 1))))
  )
)

(defun merge-list(x y)
  (cond ((eq x nil) y)
	((eq y nil) x)
	( t (cons (car x) (cons (car y) (merge-list (cdr x) (cdr y)))))
  )
)

(defun rev (x)
  (cond ((atom x) x)
	( t (append (rev(cdr x)) (cons (car x) nil)))
  )
)

(defun censor-word (w)
     (cond ((member w '(extension algorithms graphics AI midterm)) 'XXXX)
	   ( t w)
     )
)

(defun censor (list)
     (mapcar #'(lambda (x) (censor-word x)) list)
)


(defun lookup (student data)
    (cond ((eq data nil) nil)
	  ((eq (car (car data)) student) (car (cdr (car data))))
	  ( t lookup (cdr data))
    )
)

(defun sum (list)
    (cond ((eq list nil) 0)
	  ( t (+ (car list) (sum (cdr list))))
    )
)

(defun len (list)
    (cond ((eq list nil) 0)
	  ( t (+ 1 (len (cdr list))))
    )
)

(defun student-avg (s)
    (cons (car s) (cons (/ (sum (car (cdr s))) (len (car (cdr s)))) nil))
)

(defun averages (data)
    (mapcar #'(lambda (s) (student-avg s)) data)
)

(defun compare-students (s1 s2)
    (cond ((< (car (cdr s1)) (car (cdr s2))))
	  ( t nil)
    )
)


(defun deep-rev (list)
  (cond ( (atom list) list)
	( (atom (car list)) (append (deep-rev (cdr list)) (cons (car list) nil)))
	(  t                (append (deep-rev (cdr list)) (cons (deep-rev (car list)) nil)))

  )
)
'-----------------------
'TESTINGFUNCTIONS
'------------------------
'SELFCHECK
(car '(inky clyde blinky pinky))
(cons 'inky (cdr '(clyde blinky pinky)))
(car (car (cdr '(inky (blinky pinky) clyde))))
(cons ( + 1 2 ) (cdr '(+ 1 2)))
(mapcar #'(lambda (x) (/ x 2)) '(1 3 5 9))
(mapcar #'(lambda (x) (car x)) '((inky 3) (blinky 1) (clyde 33)))
(mapcar #'(lambda (x) (cdr x)) '((inky 3) (blinky 1) (clyde 33)))
'ListLength
(list-length '(A B C D))
'Doubling
(double '(1 2 3))
(double2 '(1 2 3))
'EfficientExponentiation
(power-eff 2 10)
'Merging
(merge-list '(1 2 3) '(A B C))
'Reversing
(rev nil)
(rev 'A)
(rev '(A B C D))
(rev '(A (B C) D))
'Censoring
(censor-word 'lisp)
(censor-word 'midterm)
(censor '(I NEED AN EXTENSION BECAUSE I HAD AN AI MIDTERM))
;; Define a variable holding the data:
(defvar grades '((Moaj (90.0 33.3))
		 (Sam (100.0 85.0 97.0))
		 (Steve (70.0 100.0)))
)
grades
(lookup 'Moaj grades)
(averages grades)
(compare-students '(Moaj 96.65) '(Sam 94.0))
(sort '(1 4 2 9 2 3) #'>)
(sort '((Moaj 61) (blarg 93) (banana 32)) #'compare-students)
(sort (averages grades) #'compare-students)
'DEEPREVERSE
(deep-rev 'A)
(deep-rev nil)
(deep-rev '(1 2 3 4 5))
(deep-rev '(A (B C) D))
(deep-rev '(1 2 ((3 4) 5)))
(quit)

