# 💬 SChat

**Configure the chat from the chat itself.** Drag its borders to resize, split it into
tabs with filters, and pull those tabs out into separate chat windows.

🚫 **No dependencies** — Fabric API is not required.
🖥️ Client-side only — servers do not need it, and it works on any server.

## 🤔 What it is for

- 📏 The vanilla chat is one fixed box: you get three sliders in the options and nothing
  else. Here you drag its borders like a window
- 🌊 On a busy server everything lands in one stream. Split it into tabs — trade, guild,
  private messages — and each keeps its own history
- 👁️ Watch several streams at once: pull tabs out into separate chat windows and place
  them anywhere on screen
- 🔁 Stop the same message repeating ten times in a row — it collapses into one line with
  a counter

---

## ✨ Features

### 📐 Resize and move

Open the chat and its borders turn into handles.

- ↔️ **Right edge** — width
- ↕️ **Top edge** — height. Solid line is the open chat, the dashed one above is the
  closed chat — separate heights, everything else shared
- 📦 **Top-right corner** — width and height at once
- 🔍 **Green corner, top left** — scale
- ✋ **Bottom edge** — move the whole chat

🖱️ **Middle click** a handle to reset it, **Shift + middle click** to remember the
current value as your new default.

### 🗂️ Tabs

A tab strip sits above the chat — or below it, that is per chat. Every tab is a filtered
view of the same message stream.

- 🔴 Unread messages appear as a count in a red outline
- 👀 A message visible on screen never raises anyone's counter — if you can read it, it
  is read
- 🙈 With the chat closed the strip shows up only when something is unread
- 🖱️ **LMB** switch · **RMB** that tab's settings, no switching · **MMB** close
- ✋ **Drag** — reorder the tab in the strip, or pull it out into a chat of its own

### 🪟 Multiple chats

Drag a tab out of the strip and it becomes its own chat window — own size, position,
tabs and settings. Drop a tab onto another chat to move it there, or drag a chat by its
bottom edge onto another one to merge them.

### ⚙️ Per-tab settings

The button in the top-right corner of every chat opens the active tab's settings.

- 🏷️ **Name**
- 🎨 **Background** — opacity of the backdrop behind messages
- 📜 **History** — 20 to 1000 messages, deeper than vanilla's 100
- 🔢 **Repeats** — identical messages arriving back to back collapse into one line with a
  counter `(x3)`. Layout and colour are yours, the button shows a live sample
- 👤 **Nickname button** — a copy button right after a player's nickname in the message.
  Off by default
- 📋 **Copy button** — copies the whole message. Off by default
- 🎯 **Only here** — messages this tab matches disappear from every other tab
- 🌐 **Servers** — keep the tab to a list of addresses, wildcards included:
  `holyworld.ru, *.holyworld.ru`. Empty means everywhere. Elsewhere the tab is hidden and
  gets no messages, the chat falls back to an available one, and your pick returns by
  itself once you are back
- 🔢 **Unread counter** — show or hide the count per tab. A hidden one never raises the
  strip while the chat is closed
- 🎨 **Tab colours** — plate colour, name colour and plate opacity, each tab on its own
- 📍 **Strip position** — above or below the chat, for the whole chat rather than one tab
- 🔍 **Filters** — six conditions in one button: contains · does not contain · starts
  with · does not start with · ends with · does not end with. **Any** or **All** decides
  how several of them add up, and ✂️ **Cut** drops the matched part from the displayed
  text, so the prefix the tab is about stops repeating on every line

📤 Every setting has a **To all** button that copies it to every tab in every chat.

---

## 📦 Supported versions

`1.20.1` `1.20.2` `1.20.3` `1.20.4` `1.20.5` `1.20.6` `1.21` `1.21.1` `1.21.2` `1.21.3`
`1.21.4` `1.21.5` `1.21.6` `1.21.7` `1.21.8` `1.21.9` `1.21.10` `1.21.11` `26.1`

⚠️ **Two limits on older versions:**

- **1.20.1 – 1.20.4** — history stays at vanilla's 100 messages
- **before 1.21.9** — text typed in the chat is lost when you open tab settings, vanilla
  only learned to preserve chat state in 1.21.9

## 🌍 Languages

🇬🇧 English · 🇷🇺 Russian — picked up from your game language.

## 📄 License

📦 **MIT** — free to use, fork and put in modpacks, no permission needed.
💸 Selling it is off the table anyway: the Minecraft EULA forbids selling mods or making
money from them.

---

# 💬 SChat (русский)

**Настройка чата прямо из чата.** Тяни границы, чтобы менять размер, разбивай чат на
вкладки с фильтрами и вытягивай вкладки в отдельные окна.

🚫 **Без зависимостей** — Fabric API не нужен.
🖥️ Только клиент — серверу мод не требуется, работает на любом сервере.

## 🤔 Зачем он нужен

- 📏 Ванильный чат — одна коробка фиксированного размера: три ползунка в настройках и
  всё. Здесь его границы тянутся, как у окна
- 🌊 На живом сервере всё валится в один поток. Раздели его на вкладки — торговля,
  гильдия, личные сообщения — у каждой своя история
- 👁️ Следи за несколькими потоками сразу: вынеси вкладки в отдельные окна чата и
  расставь их по экрану
- 🔁 Не смотри, как одно и то же сообщение повторяется десять раз подряд — оно
  схлопнется в одну строку со счётчиком

---

## ✨ Возможности

### 📐 Размер и положение

Открой чат — границы станут ручками.

- ↔️ **Правый край** — ширина
- ↕️ **Верхний край** — высота. Сплошная линия — открытый чат, пунктирная над ней —
  закрытый: высота у них разная, всё остальное общее
- 📦 **Правый верхний угол** — ширина и высота сразу
- 🔍 **Зелёный уголок слева сверху** — масштаб
- ✋ **Нижний край** — перенос всего чата

🖱️ **СКМ** по ручке сбрасывает её, **Shift + СКМ** запоминает текущее значение как твой
новый дефолт.

### 🗂️ Вкладки

Над чатом появляется полоса вкладок — или под ним, это настраивается у каждого чата.
Каждая вкладка — отфильтрованный вид одного потока сообщений.

- 🔴 Непрочитанные показываются числом в красной рамке
- 👀 Сообщение, видное на экране, ничей счётчик не поднимает — читаешь, значит прочитано
- 🙈 При закрытом чате полоса появляется, только если что-то непрочитано
- 🖱️ **ЛКМ** переключить · **ПКМ** настройки этой вкладки, не переключаясь · **СКМ**
  закрыть
- ✋ **Потянуть** — поменять порядок вкладок в полосе или вынести вкладку в отдельный чат

### 🪟 Несколько чатов

Вытяни вкладку из полосы — получишь отдельное окно чата со своим размером, позицией,
вкладками и настройками. Брось вкладку на другой чат — переедет туда; потяни чат за
нижнюю кромку на другой — сольются.

### ⚙️ Настройки вкладки

Кнопка в правом верхнем углу каждого чата открывает настройки активной вкладки.

- 🏷️ **Имя**
- 🎨 **Фон** — прозрачность подложки под сообщениями
- 📜 **История** — от 20 до 1000 сообщений, глубже ванильной сотни
- 🔢 **Повторы** — одинаковые сообщения подряд схлопываются в одну строку со счётчиком
  `(x3)`. Вид и цвет — твои, на кнопке живой образец
- 👤 **Кнопка ника** — кнопка копирования сразу за ником игрока в сообщении. По
  умолчанию выключена
- 📋 **Кнопка копирования** — копирует сообщение целиком. По умолчанию выключена
- 🎯 **Только здесь** — сообщения этой вкладки исчезают из всех остальных
- 🌐 **Серверы** — где вкладке жить: список адресов, в том числе с масками,
  `holyworld.ru, *.holyworld.ru`. Пусто — везде. На чужом сервере вкладка скрыта и
  сообщений не получает, чат показывает доступную, а твой выбор возвращается сам, как
  только ты вернулся
- 🔢 **Счётчик** — показывать или скрыть число непрочитанных у вкладки. Скрытый не
  поднимает полосу при закрытом чате
- 🎨 **Цвета вкладки** — цвет плашки, цвет названия и прозрачность плашки, у каждой свои
- 📍 **Полоса вкладок** — над чатом или под ним, настройка всего чата, а не одной вкладки
- 🔍 **Фильтры** — шесть условий в одной кнопке: содержит · не содержит · начинается с ·
  не начинается с · заканчивается на · не заканчивается на. **Любое** или **Все** решает,
  как складываются несколько условий, а ✂️ **Вырезать** убирает совпавшую часть из
  показанного текста — префикс, ради которого заведена вкладка, не повторяется в каждой
  строке

📤 У каждой настройки есть кнопка **Всем** — копирует её всем вкладкам во всех чатах.

---

## 📦 Поддерживаемые версии

`1.20.1` `1.20.2` `1.20.3` `1.20.4` `1.20.5` `1.20.6` `1.21` `1.21.1` `1.21.2` `1.21.3`
`1.21.4` `1.21.5` `1.21.6` `1.21.7` `1.21.8` `1.21.9` `1.21.10` `1.21.11` `26.1`

⚠️ **Два ограничения на старых версиях:**

- **1.20.1 – 1.20.4** — история остаётся ванильной сотней сообщений
- **до 1.21.9** — набранный в чате текст теряется при открытии настроек вкладки:
  сохранять состояние чата ваниль научилась только в 1.21.9

## 🌍 Языки

🇬🇧 Английский · 🇷🇺 Русский — по языку игры.

## 📄 Лицензия

📦 **MIT** — используй, форкай и клади в сборки, разрешение не нужно.
💸 Продать всё равно не выйдет: EULA Minecraft запрещает продавать моды и зарабатывать
на них.
