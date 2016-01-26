# Запуск WSImport #

wsimport -extension -target 2.0 -keep -p ru.gt2.rusref.fias.update http://fias.nalog.ru/WebServices/Public/DownloadService.asmx?wsdl

Используем target 2.0, чтобы запускалось на Java SE6 без дополнительных телодвижений.