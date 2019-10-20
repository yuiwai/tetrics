# tetrics

Scalaによる、4方向テトリス実装です。中央のフィールドでブロックを回転・移動させたのち、上下左右のいずれかのフィールドに落として遊びます。

現時点では以下の実装があります。

- libGDXを経由したAndroid版
- scala-jsを経由したブラウザ(Canvas)版
- scala-nativeを経由したターミナル版

## Android版

[こちら](https://play.google.com/store/apps/details?id=com.yuiwai.tetrics)で公開しています。

操作方法はアプリ内のヘルプをご確認下さい。

## スマホブラウザ版

スマホ版実装は以下から遊べます

https://lab.yuiwai.com/tetrics/multitouch.html

### How to play

スマホ版は画面を左右半分に分け、それぞれに操作が割り当てられています

- 左半分は、ブロックの移動
  - スワイプすることで、任意の方向にブロックを移動できます
- 右半分は、ブロックの回転
  - 左右にスワイプすることで、ブロックを回転できます
- 左右同時にタッチしてブロックをドロップ
  - 左半分をタッチしながら右半分でスワイプすることで、任意の方向にブロックをドロップできます

> ドロップする場所を決めたら先に回転したのち、移動 -> ドロップするとスムーズです

## PCブラウザ版

ブラウザ版実装は以下から遊べます（要キーボード）

https://lab.yuiwai.com/tetrics/

### How to play

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

## ターミナル版

cloneしてsbtで実行ファイルを生成し、実行します。

注）[scala-native](http://www.scala-native.org/en/v0.3.9-docs/)のセットアップとlibncursesが必要です。

```
$ sbt native/nativeLink
$ ./native/target/scala-2.11/tetrics-native-out
```

操作方法はPC版と同じです。