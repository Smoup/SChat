## 1.0.4

- Fixed: switching between servers no longer drops you with a network protocol error — the
  chat was rebuilt while the game was still restoring its message history, and the crash hit
  whenever a server-bound tab was the active one
- Fixed: the tab you picked survives the switch. While it is unavailable the panel falls back
  to another one, but your choice is no longer overwritten and the panel returns to it by
  itself once the tab is available again
- Fixed: filtered messages no longer end up in the "All" tab after a switch — tabs that claim
  messages are re-checked as soon as the address of the new server is known

## 1.0.3

- Fixed: chat frames and handles no longer cover the input line and the command
  suggestion list — the whole overlay is now drawn behind the chat screen
- Fixed: clicking a command suggestion no longer grabs a panel border and starts a resize
- Fixed: the tab strip of a closed chat now pops up only for the panel that actually has
  unread messages, not for every panel at once
- Fixed: hover hints are drawn on top of the frames instead of sliding under them
- The server list accepts addresses in quotes and brackets, so a block copied straight
  out of a JSON array works as is

## 1.0.2

- **Server binding**: a tab can be limited to a list of addresses, wildcards included —
  `holyworld.ru, *.holyworld.ru`. Elsewhere the tab is hidden and receives no messages
- **Tab colours**: separate colours for the plate and for the name, plus plate opacity
- **Unread counter** can be turned off per tab; a hidden counter no longer raises the tab
  strip while the chat is closed
- Tabs can be reordered by dragging them within the strip
- The chat now sits flush with the bottom while closed and lifts above the input line
  while open, tab strip included
- Chat borders are readable against a bright sky: solid outline got a dark backing, the
  dashed one turned amber
- Fixed: dragging a tab out no longer snaps the new chat to the cursor by its corner
- Fixed: a new tab inherits colours and opacity from the first one

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

## 1.0.4 (русский)

- Исправлено: переход между серверами больше не выкидывает с ошибкой сетевого протокола —
  чат перестраивался прямо во время того, как игра восстанавливала историю сообщений, и
  краш ловился, когда активной была вкладка с привязкой к серверам
- Исправлено: выбранная вкладка переживает переход. Пока она недоступна, панель показывает
  другую, но сам выбор больше не перезаписывается, и панель возвращается к нему сама, как
  только вкладка снова доступна
- Исправлено: отфильтрованные сообщения больше не попадают во вкладку «Всё» после перехода —
  вкладки, забирающие сообщения себе, пересчитываются, как только известен адрес нового
  сервера

## 1.0.3 (русский)

- Исправлено: рамки и ручки чата больше не перекрывают строку ввода и список подсказок
  команд — весь оверлей рисуется за экраном чата
- Исправлено: клик по подсказке команды больше не цепляет границу панели и не начинает
  изменение размера
- Исправлено: полоса вкладок у закрытого чата всплывает только у той панели, где
  действительно есть непрочитанное, а не у всех сразу
- Исправлено: подсказки при наведении рисуются поверх рамок, а не под ними
- Список серверов принимает адреса в кавычках и скобках — кусок, скопированный прямо из
  JSON-массива, работает как есть

## 1.0.2 (русский)

- **Привязка к серверам**: вкладку можно ограничить списком адресов, в том числе с
  масками — `holyworld.ru, *.holyworld.ru`. На других серверах вкладка скрыта и
  сообщения в неё не идут
- **Цвета вкладки**: отдельно цвет плашки и цвет названия, плюс прозрачность плашки
- **Счётчик непрочитанных** отключается у каждой вкладки; скрытый счётчик больше не
  поднимает полосу вкладок при закрытом чате
- Вкладки можно менять местами перетаскиванием внутри полосы
- Чат прижимается к самому низу, пока закрыт, и поднимается над строкой ввода, когда
  открыт — вместе с полосой вкладок
- Границы чата видно на светлом небе: у сплошной рамки появилась тёмная подложка,
  пунктир стал жёлтым
- Исправлено: вынесенная вкладка больше не прыгает к курсору нижним левым углом
- Исправлено: новая вкладка перенимает цвета и прозрачность у первой

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
