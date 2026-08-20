## 1.0.1

- Filter mode switch: **Any** (one match is enough) or **All** (the message must satisfy
  every condition at once, e.g. start with one word and end with another)
- **Cut** toggle on every filter: removes the matched part from the displayed text, so a
  prefix the whole tab is about no longer repeats on every line
- Tab strip can be placed **above or below** the chat
- Right click a tab to open its settings without switching to it
- Scrollbar next to the filter list
- Fixed: "Only here" now hides messages from tabs in **all** chats, not just its own
- Fixed: cutting works even when the server splits the message into differently styled
  pieces

## 1.0.0

First release.

- Resize the chat by dragging its borders: width, height, scale and position. The open
  and the closed chat keep separate heights, everything else is shared
- Middle click a handle to reset it, Shift + middle click to save the current value as
  your default
- Tabs above the chat, each a filtered view of the same message stream, with its own
  history, background opacity and unread counter
- Pull a tab out to get a separate chat window; drop tabs onto other chats or merge
  whole chats together
- Filters with six conditions: contains, does not contain, starts with, does not start
  with, ends with, does not end with
- Identical messages arriving back to back collapse into one line with a counter
- Optional buttons to copy a player nickname or the whole message
- Per-tab history up to 1000 messages, deeper than vanilla's 100
- English and Russian, no Fabric API required

---

## 1.0.1 (русский)

- Переключатель режима фильтров: **Любое** (хватит одного совпадения) или **Все**
  (сообщение должно подойти сразу всем условиям, например начинаться с одного слова и
  заканчиваться другим)
- Кнопка **Вырезать** у каждого фильтра: убирает совпавшую часть из показанного текста,
  чтобы префикс, ради которого и заведена вкладка, не повторялся в каждой строке
- Полосу вкладок можно поставить **над чатом или под ним**
- ПКМ по вкладке открывает её настройки, не переключая на неё чат
- Полоса прокрутки рядом со списком фильтров
- Исправлено: «Только здесь» убирает сообщения из вкладок **во всех чатах**, а не только
  в своём
- Исправлено: вырезание работает и когда сервер разбивает сообщение на куски с разными
  стилями

## 1.0.0 (русский)

Первый релиз.

- Размер чата тянется за границы: ширина, высота, масштаб и позиция. Открытый и
  закрытый чат держат разную высоту, всё остальное общее
- СКМ по ручке сбрасывает её, Shift + СКМ запоминает текущее значение как дефолт
- Вкладки над чатом: каждая — отфильтрованный вид одного потока сообщений со своей
  историей, прозрачностью фона и счётчиком непрочитанных
- Вкладку можно вынести в отдельное окно чата, перенести в другой чат или слить чаты
- Фильтры с шестью условиями: содержит, не содержит, начинается с, не начинается с,
  заканчивается на, не заканчивается на
- Одинаковые сообщения подряд схлопываются в одну строку со счётчиком
- Кнопки копирования ника и всего сообщения (по умолчанию выключены)
- История до 1000 сообщений на вкладку вместо ванильной сотни
- Английский и русский, Fabric API не требуется
