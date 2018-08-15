# tetrics

Scalaによる、4方向テトリス実装です。中央のフィールドでブロックを回転・移動させたのち、上下左右のいずれかのフィールドに落として遊びます。

現時点ではscala-jsを経由したブラウザ(Canvas)版実装と、scala-nativeを経由したネイティブ(ターミナル)版実装があります。

## Demo

ブラウザ版実装は以下から遊べます（要キーボード）

https://lab.yuiwai.com/tetrics/

## How to play

Move Right: L  
Move Left: H  
Move Up: K  
Move Down: J  
Drop Right: Shift + L  
Drop Left: Shift + H  
Drop Top: Shift + K  
Drop Bottom: Shift + J  
Rotate Right: F  
Rotate Left: D  

## 構成

tetricsは以下のプロジェクト構成となっています。

- core ... 動作環境に依存しない、共通の処理
- js ... JS版の実装。coreに依存
- native ... Native版の実装。coreに依存

coreの内部は大きく分けると以下で構成されています。

- Tetrics ... 4方向テトリスを実現するロジック部分。状態と振る舞いを持つ
- TetricsView ... 描画に関する処理を抽象化したtrait
- TetricsController ... 入力に関する処理を抽象化したtrait
- TetricsEvent ... 状態の変化に伴って送出されるイベントに関する処理を担当
- TetricsGame ... 上記の構成要素を集約し、具体的なルールを持ったゲームとして定義するためのtrait

以下、順に見ていきます。

### Tetrics

ロジックの中核を成す部分。以下のクラスから構成されています。

- Tetricsクラス ... 配下の構成要素を集約したクラスであり、外とのやり取りをおこなうインターフェース。
- Fieldクラス ... Blockを配置するフィールドを表す。4方向+中央で5つのフィールドが存在し、親であるTetricsの状態として保持されている。
- Blockクラス ... ユーザが操作し、積んでいくブロックを表す。中央フィールド内で回転・移動し、4方向のフィールドにドロップされる。
- Rowクラス ... フィールド内に配置されたブロックの横一行分のデータを表す。ブロックは複数行になり得るため、Rowのリストとして表現される。

### TetricsView

状態を受け取り画面に描画する責務を抽象化したtrait。tetricsにおける「状態」はTetricsクラスに集約されているため、このインスタンスを受け取ることで一意な結果を描画出来ます。

```
trait TetricsView[C]
```

このtraitは、Cという型パラメータをひとつ取っていますが、これは描画処理の具象実装におけるコンテキストを表しています（たとえば、JS版のCanvas実装におけるCanvasRenderingContext2D）

### TetricsController


### TetricsEvent


### TetricsGame

