# YrC Base Functions:
## How to set variable?
```
a = "1"
b = a
b += "1"
b -= "1"
b /= "1"
b *= "1"
```
## How to create final variable?(Only on YrC Native)
```
!a = "a"
!b = a
```
## How to join two strings?
```
a = "Count:"
b = "1"
strCat a and b to a
print a
```
## How to print?
```
print "123"
a = "123"
print a
```
## How to parse http(s)?
```
net_get_contents "https://google.com" a
print a
```
## How to unset variable?
```
a = "123"
delete a
```
## How to stop code execution?
```
return
```
* example from `Function`:
```
new Function func
-print "1"
-if func.args.0 == "1" cast return
-print "2"
END.func
func "1"
print "3"
```
_Output: 13_
## How to use IF?
```
new Function ifTrue
-print "TRUE!"
END.ifTrue

new Function ifFalse
-print "Nea! Its False"
END.ifTrue

a = "1"
b = "2"

if a == b cast ifTrue
if a != b cast ifFalse
```
or
```
a = "1"
b = "2"

if a == b cast print "yes"
if a != b cast print "no"
```
* **`cast` - Allowed only to functions!**

## What /SP - /RE?
/RE = "/n" (Return symbols)

/SP = " " (Space symbols)

# YrC Constructors
## Functions
### How to create custom function?
```
new Function myfunc
-print "This/SPis/SPmy/SPfunction!"
END.myfunc
```
### How to cast my function?
```
myfunc
```
