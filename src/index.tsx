import {DeviceEventEmitter, NativeModules, Platform} from 'react-native';

const LINKING_ERROR =
    `The package 'react-native-ilab-library-demo' doesn't seem to be linked. Make sure: \n\n` +
    Platform.select({ios: "- You have run 'pod install'\n", default: ''}) +
    '- You rebuilt the app after installing the package\n' +
    '- You are not using Expo managed workflow\n';

let emitter: any = null

const ILabLibraryDemo = NativeModules.IlabLibraryDemo
    ? NativeModules.IlabLibraryDemo
    : new Proxy(
        {},
        {
            get() {
                throw new Error(LINKING_ERROR);
            },
        }
    );

export function addReadCardCallback(callback: any) {
    emitter = DeviceEventEmitter.addListener('cardId', data => {
        callback && callback(data)
    })
}

export async function readCard() {
    return ILabLibraryDemo.readCard()
}

export function releaseReadCard() {
    emitter.remove()
    ILabLibraryDemo.release()
}
