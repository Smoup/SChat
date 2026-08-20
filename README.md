# SChat

Client-side Fabric mod that turns the vanilla chat into a configurable one — resize it,
split it into tabs with filters, and pull those tabs out into separate chat windows.
Everything is set up from the chat itself: no config files to edit, no mod menu to install.

**No dependencies.** Fabric API is not required.

---

## Features

### Resize and position

Open the chat and the borders become handles.

- **Right edge** — width
- **Top edge** — height. The solid line is the open chat, the dashed one above it is the
  closed chat: they keep separate heights but share everything else
- **Top-right corner** — width and height at once
- **Green corner, top left** — scale
- **Bottom edge** — move the whole chat

Everything you drag is saved and synced with the vanilla chat settings.

| Action | Result |
|---|---|
| Middle click on a handle | reset that setting |
| Shift + middle click | remember the current value as the new default |

### Tabs

A strip of tabs sits above the chat. Each tab is a filtered view of the same message
stream, with its own name, look and history.

- Unread messages show up as a count in a red outline. A message that is visible on
  screen never raises anyone's counter — if you can read it, it is read
- The strip is always visible while the chat is open; when the chat is closed it appears
  only if something is unread
- **Left click** — switch, **middle click** — close, **drag** — pull the tab out

### Multiple chats

Drag a tab out of the strip and it becomes a separate chat window with its own size,
position, tabs and settings. Drop a tab onto another chat to move it there; drag a chat
by its bottom edge onto another one to merge them.

The last tab of the main chat cannot be closed or pulled out — the chat would be left
with nothing to show.

### Per-tab settings

The button in the top-right corner of every chat opens the settings of the active tab.

- **Name**
- **Background** — opacity of the black backdrop behind messages
- **History** — 20 to 1000 messages, deeper than the vanilla 100
- **Repeats** — identical messages arriving back to back collapse into one line with a
  counter. Layout and colour are configurable, the button shows a live sample
- **Nickname button** — appends a copy button right after a player nickname found in the
  message. Off by default: AdminTool draws the same button, and a second one is never
  inserted
- **Copy button** — copies the whole message. The repeat counter never reaches the
  clipboard
- **Only here** — messages this tab matches disappear from every other tab, including
  the catch-all one
- **Filters** — six conditions in one button: contains, does not contain, starts with,
  does not start with, ends with, does not end with. Case and colour codes are ignored

Every setting has a **To all** button that copies it to every tab in every chat.

---

## Supported versions

`1.20.1` `1.20.2` `1.20.3` `1.20.4` `1.20.5` `1.20.6` `1.21` `1.21.1` `1.21.2` `1.21.3`
`1.21.4` `1.21.5` `1.21.6` `1.21.7` `1.21.8` `1.21.9` `1.21.10` `1.21.11` `26.1`

Two limits on older versions:

- **1.20.1 – 1.20.4**: history stays at the vanilla 100 messages — there is nothing to
  raise the limit on yet
- **before 1.21.9**: text typed into the chat is lost when the tab settings are opened,
  because vanilla only learned to preserve the chat state in 1.21.9

## Languages

English and Russian, picked up from the game language setting.

## Building

```
./gradlew build              # every version, jars in build/libs/<version>/
./gradlew :1.21.11:build     # a single version
./gradlew :1.21.11:runClient # dev client
```

---

# SChat (русский)

Клиентский Fabric-мод, который превращает ванильный чат в настраиваемый: чат можно
растягивать, разбивать на вкладки с фильтрами и вытягивать эти вкладки в отдельные окна.
Всё настраивается прямо из чата — без правки конфигов и без мода настроек.

**Без зависимостей.** Fabric API не нужен.

---

## Возможности

### Размер и положение

Открой чат — и его границы станут ручками.

- **Правый край** — ширина
- **Верхний край** — высота. Сплошная линия — открытый чат, пунктирная над ней —
  закрытый: у них разная высота, всё остальное общее
- **Правый верхний угол** — ширина и высота сразу
- **Зелёный уголок слева сверху** — масштаб
- **Нижний край** — перенос всего чата

Всё, что перетянул, сохраняется и синхронизируется с ванильными настройками чата.

| Действие | Результат |
|---|---|
| СКМ по ручке | сброс этой настройки |
| Shift + СКМ | запомнить текущее значение как новый дефолт |

### Вкладки

Над чатом появляется полоса вкладок. Каждая вкладка — это отфильтрованный вид одного и
того же потока сообщений, со своим именем, оформлением и историей.

- Непрочитанные показываются числом в красной рамке. Сообщение, которое видно на экране,
  ничей счётчик не поднимает — если ты его читаешь, оно прочитано
- Полоса всегда видна при открытом чате; при закрытом — только если что-то непрочитано
- **ЛКМ** — переключить, **СКМ** — закрыть, **потянуть** — вынести вкладку

### Несколько чатов

Вытяни вкладку из полосы — получишь отдельное окно чата со своим размером, позицией,
вкладками и настройками. Брось вкладку на другой чат — она переедет туда; потяни чат за
нижнюю кромку на другой — они сольются.

Последнюю вкладку главного чата нельзя ни закрыть, ни вынести: чату станет нечего
показывать.

### Настройки вкладки

Кнопка в правом верхнем углу каждого чата открывает настройки активной вкладки.

- **Имя**
- **Фон** — прозрачность чёрной подложки сообщений
- **История** — от 20 до 1000 сообщений, глубже ванильной сотни
- **Повторы** — одинаковые сообщения, пришедшие подряд, схлопываются в одну строку со
  счётчиком. Вид и цвет настраиваются, на кнопке — живой образец
- **Кнопка ника** — дописывает кнопку копирования сразу за ником игрока, найденным в
  сообщении. По умолчанию выключена: такую же кнопку рисует AdminTool, и второй мод
  никогда не вставит
- **Кнопка копирования** — копирует сообщение целиком. Счётчик повторов в буфер не
  попадает
- **Только здесь** — сообщения, подошедшие этой вкладке, исчезают из всех остальных, в
  том числе из общей
- **Фильтры** — шесть условий в одной кнопке: содержит, не содержит, начинается с, не
  начинается с, заканчивается на, не заканчивается на. Регистр и цветовые коды не
  учитываются

У каждой настройки есть кнопка **Всем**, которая копирует её всем вкладкам во всех чатах.

---

## Поддерживаемые версии

`1.20.1` `1.20.2` `1.20.3` `1.20.4` `1.20.5` `1.20.6` `1.21` `1.21.1` `1.21.2` `1.21.3`
`1.21.4` `1.21.5` `1.21.6` `1.21.7` `1.21.8` `1.21.9` `1.21.10` `1.21.11` `26.1`

Два ограничения на старых версиях:

- **1.20.1 – 1.20.4**: история остаётся ванильной сотней сообщений — поднимать лимит там
  ещё негде
- **до 1.21.9**: набранный в чате текст теряется при открытии настроек вкладки, потому
  что сохранять состояние чата ваниль научилась только в 1.21.9

## Языки

Английский и русский, выбираются по языку игры.

## Сборка

```
./gradlew build              # все версии, jar-ы в build/libs/<версия>/
./gradlew :1.21.11:build     # одна версия
./gradlew :1.21.11:runClient # dev-клиент
```
