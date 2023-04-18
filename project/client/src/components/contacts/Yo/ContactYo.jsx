import React, {useEffect, useReducer, useRef} from "react";
import {Helmet} from "react-helmet-async";
import contactCSS from '../contactMain.module.css';
import {contacts, states} from "../../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import {chStatB, errorLoad, getEdCon, setActNew, setTyp} from "../ContactMain";
import ErrFound from "../../other/error/ErrFound";
import {CHANGE_EVENTS_CLEAR, changeEvents} from "../../../store/actions";
import {prefSite} from "../../main/Main";

let dispatch, contactsInfo, type, cState, inps, errText;
type = "Yo";
inps = {};
errText = "К сожалению, информация не найдена... Можете попробовать попросить завуча заполнить информацию.";
let [_, forceUpdate] = [];

export function ContactYo() {
    contactsInfo = useSelector(contacts);
    cState = useSelector(states);
    if(!dispatch) {
        setActNew(1);
        setTyp(type);
    }
    [_, forceUpdate] = useReducer((x) => x + 1, 0);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount ContactYo.jsx");
        setTyp(type);
        for(let el of document.querySelectorAll("." + contactCSS.ed + " > *[id^='inpn']")){
            chStatB({target: el}, inps);
        }
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            dispatch = undefined;
            console.log("I was triggered during componentWillUnmount ContactYo.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate ContactYo.jsx');
    });
    return (
        <div className={contactCSS.header}>
            <Helmet>
                <title>Контакты учебного центра</title>
            </Helmet>
            {(!contactsInfo[type].contact && !contactsInfo[type].mapPr && !(cState.auth && cState.role == 3)) ?
                    <ErrFound text={errText}/>
                :
                    <div className={contactCSS.block}>
                        {(cState.auth && cState.role == 3) ?
                                getEdCon(contactsInfo, inps, forceUpdate)
                            :
                                <section className={contactCSS.center_colum}>
                                    <div className={contactCSS.blockTel}>
                                        <h1 className={contactCSS.zag}>Телефоны для связи</h1>
                                        <pre className={contactCSS.field}>
                                            {contactsInfo[type].contact}
                                        </pre>
                                    </div>
                                    <div className={contactCSS.map+" "+contactCSS.blockTel}>
                                        <h1 className={contactCSS.zag}>Карта проезда</h1>
                                        <pre className={contactCSS.field}>
                                            {contactsInfo[type].mapPr.text}
                                        </pre>
                                        <span className={contactCSS.banner}>
                                            <img alt="banner" src={prefSite+contactsInfo[type].mapPr.imgUrl+''} onError={errorLoad}/>
                                        </span>
                                    </div>
                                </section>
                        }
                    </div>
            }
        </div>
    )
}
export default ContactYo;