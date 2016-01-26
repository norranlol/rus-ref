#summary Настройка MySQL

# Установка на Windows #

5.5.23 для 64-х битных систем.
Windows (x86, 64-bit), MSI Installer

MSI — для упрощения процесса обновления — достаточно просто запустить новую версию MSI и просто нажать на Upgrade.
По крайней мере с 5.5.22 до 5.5.23 я успешно обновился именно таким образом.

## Параметры установки ##

Сервер в C:\App\MySQL
Данные в C:\Data\MySQL

  * [x](x.md) MySQL Server
    * [x](x.md) Client Programs
    * [ ] Debug binaries
  * [ ] Development Components
    * [ ] Client C API library (shared)
    * [ ] Embedded server library
  * [ ] Debug Symbols
  * [x](x.md) Server data files

## Конфигурирование ##

(**) Detailed Configuration
(**) Developer Machine
(**) Multifunctional Database
[C: ](.md) [Installation Path ](.md)
(**) Decision Support (DSS)/OLAP
[x](x.md) Enable TCP/IP Networking
Port Number [3306 ](.md) [x](x.md) Add firewall exception for this port
[x](x.md) Enable Strict Mode
(**) Best Support for Multilingualism
[x](x.md) Install As Windwos Service
[ ] Launch the MySQL Server automatically (если надо сами запустим)
[x](x.md) Modify Sercurity Settings**

## Редактирование my.ini ##

innodb\_additional\_mem\_pool\_size=16M

innodb\_log\_buffer\_size=8M

innodb\_buffer\_pool\_size=2G

innodb\_log\_file\_size=1G

## Подключние в phpMyAdmin ##

Создаём пользователя — запускаем консольного клиента:

CREATE USER 'phpmyadmin'@'%' IDENTIFIED BY 'phpmyadminpassword';
GRANT ALL PRIVILEGES ON **.** TO 'phpmyadmin'@'%' WITH GRANT OPTION;

Плюс example/create\_tables.sql

# Worklog #

## 2012-04-13 22:23 MSK ##

Да, уж MySQL несмотря на настройки уже второй день лопатит получившиеся csv-шки.
Что совсем никуда не годиться. Нужно оптимизировать. И оптимизировать жёстко.
Всё-таки 8 Гб оперативки на машине.

## 2012-04-13 23:11 MSK ##

Ну что, поставили, сконфигурировали… полетели?
cd c:\projects\rus-ref\fias\data\2012-03-22-xml
C:\App\MySQL\bin\mysql.exe --user=root --password=root < ..\..\src\main\sql\mysql\create.sql
C:\App\MySQL\bin\mysql.exe --user=root --password=root --local --show-warnings fias < ..\..\src\main\sql\mysql\load.sql

## 2012-04-13 23:45 MSK ##

Да, так-то оно гораздо шустрей получилось. Час (ну максимум два) — и будет всё готово.

## 2012-04-14 23:37 MSK ##

Посмотрел презентацию про InnoDB, понял что я немного неправ с Primary Key. В смысле использования GUID'ов.
Собтсвенно пока вижу два варианта — добавлять суррогатный Autoincrement и предварительно отсортировать полученные csv'шки по первичному ключу (для чего он должен идти первой колонкой в файле).
Хотя второй вариант можно реализовать и на стороне java — использовать два прохода для записи, в первом набрать таблицу… Хотя нет как-то очень сложно.

Для начала попробуем какая будет скорость вставок при включении буфера нормального размера.
Посмотрим с какой скоростью запишется отсортированный NormativeDocument.

Да, похоже сортировка творит чудеса.

## 2012-04-15 07:32 MSK ##

Да, получили вставку в NormativeDocument на «дохленьком» комьютере за 11 минут и 43.89 секунд. Плюс ещё 39.34 секунд на коммит.