import React, {useEffect, useReducer, useRef} from "react";
import paneCSS from './pane.module.css';
import {Link} from "react-router-dom";
import {useDispatch, useSelector} from "react-redux";
import {groups, pane, states} from "../../../store/selector";
import {
    CHANGE_EVENTS_STEP,
    CHANGE_PANE,
    CHANGE_PANE_DEL_GRS,
    CHANGE_PANE_GR,
    CHANGE_PANE_GRS,
    changeEvents,
    changeGroups
} from "../../../store/actions";
import PanJs from "./PanJs";
import yes from "../../../media/yes.png";
import no from "../../../media/no.png";
import ed from "../../../media/edit.png";
import {addGroup, chGroup, remGroup} from "../../people/PeopleMain";

let kel, gType;
kel = 0;
gType = {
    true : {
        CHANGE_PANE: "CHANGE_GROUPS",
        CHANGE_PANE_DEL_GRS: "CHANGE_GROUPS_DEL_GRS",
        CHANGE_PANE_GR: "CHANGE_GROUPS_GR",
        CHANGE_PANE_GRS: "CHANGE_GROUPS_GRS",
    },
    false: {
        CHANGE_PANE: "CHANGE_PANE",
        CHANGE_PANE_DEL_GRS: "CHANGE_PANE_DEL_GRS",
        CHANGE_PANE_GR: "CHANGE_PANE_GR",
        CHANGE_PANE_GRS: "CHANGE_PANE_GRS",
    }
}

export function Pane(props) {
    const ele = (x, par, b) => {
        if(b){
            if(!panJs.inps[par]) panJs.inps[par] = x;
        } else {
            panJs.pari[par] = x;
        }
    };
    const getPan = (name, namecl, link, dopClass, fun, inc) => {
        let cl = "pan" + namecl;
        if (!inc) panJs.pari.elems++;
        let cla = [paneCSS.nav_i, paneCSS.nav_iJur, "pa", cl, dopClass ? dopClass : ""].join(" ");
        return link ?
            <Link className={cla} id={paneCSS.nav_i} to={link} onClick={fun} data-id={namecl} key={namecl}>
                {name.nam}
            </Link>
            :
            <div className={cla} id={paneCSS.nav_i} onClick={fun} data-id={namecl} key={namecl} data-st="0">
                {props.cla && cState.role == 3 ?
                    <>
                        <div className={paneCSS.field+" "+paneCSS.fi}>
                            {name}
                        </div>
                        <img className={paneCSS.imgfield+" "+paneCSS.fi} src={ed} onClick={onEdGr} title="Редактировать" alt=""/>
                        <img className={paneCSS.imginp+" "+paneCSS.fi} src={no} onClick={onDel} title="Удалить группу" alt=""/>
                    </> : name
                }
            </div>
    };
    const overpan = () => {
        let wid, pa, add;
        add = (props.cla && cState.role == 3) ? "15% ": "";
        panJs.eles = [];
        pa = panJs.nav.querySelectorAll("#pan > .pa");
        for(let pae of pa) {
            if(pae.style.display) pae.style.display = "";
        }
        panJs.mor.style.display = "none";
        if(panJs.refes.lin) {
            panJs.refes.lin.style.display = "none";
        }
        panJs.nav.style.gridTemplate = "7vh/ " + add + "repeat(" + panJs.pari.elems + ",1fr)";
        wid = panJs.nav.scrollWidth - panJs.nav.getBoundingClientRect().width;
        if(panJs.refes.lin) {
            panJs.refes.lin.style.display = "";
        }
        if(wid > 1) {
            let i = -1, bol;
            bol = (props.cla && cState.role == 3) ? 3 : 1;
            for(let el, i1 = 0, elc; wid > 1 || i1 < bol; i--) {
                if(wid < -1) i1++;
                el = pa[pa.length+i];
                wid -= el.getBoundingClientRect().width;
                elc = panJs.gr[el.getAttribute("data-id")];
                panJs.eles[panJs.eles.length] = React.cloneElement(elc, {className: elc.props.className+" "+paneCSS.pred});
                el.style.display = "none";
                panJs.pari.elems--;
            }
            panJs.lel = pa[pa.length+i];
            if(panJs.refes.MMel) {
                let bol = panJs.lel.getBoundingClientRect().width < 50;
                panJs.refes.MMel.style.minWidth = bol ? "200%" : "100%";
                panJs.refes.MMel.style.marginRight = bol ? "200%" : "100%";
            }
            updMor();
        }
        panJs.nav.style.gridTemplate = "7vh/ " + add + "repeat(" + panJs.pari.elems + ",1fr)";
        setGr();
    };
    const setGr = (bol) => {
        let nam = ".pan" + panJs.info.group;
        if(panJs.blockCl == "DEL"){
            panJs.blockCl = false;
            return;
        }
        if(panJs.info.groups[panJs.info.group]){
            if (panJs.act != nam || !bol) setActivedMy(nam);
        } else {
            if(props.main) {
                setActivedMy(undefined);
                return;
            }
            let grps = Object.getOwnPropertyNames(panJs.info.groups);
            if(grps.length == 0){
                if(panJs.refes.lin) panJs.refes.lin.style.width = "0";
                return;
            }
            setActivedMy(".pan" + grps[0]);
        }
    };
    const updMor = () => {
        if(panJs.eles.length > 0) {
            panJs.lmor = getMore(panJs.eles);
            panJs.parb.updf = true;
            panJs.mor.style.display = "flex";
            forceUpdate();
        }
    };
    const getMore = (el) => {
        panJs.pari.elems++;
        let bol = panJs.lel.getBoundingClientRect().width < 50;
        return <>
                <div className={paneCSS.nav_i+' '+paneCSS.nav_iJur+' '+paneCSS.predEl} id={paneCSS.nav_i}>
                    <div className={paneCSS.predInf}>...</div>
                </div>
                <div className={paneCSS.predMenu+" pre "+paneCSS.predMM} style={{minWidth:bol ? "200%" : "100%", marginRight: bol ? "200%" : "100%"}} id="MM" ref={(re)=>(panJs.refes.MMel = re)}>
                    <div>
                        {el.map(par => par)}
                    </div>
                </div>
        </>
    };
    const replGr = (x) => {
        let elc = panJs.gr[panJs.lel.getAttribute("data-id")];
        let elr = React.cloneElement(elc, {className: elc.props.className+" "+paneCSS.pred});
        for (let i = 0; i < panJs.eles.length; i++) {
            if(panJs.eles[i].props["data-id"] == x.getAttribute("data-id")) {
                panJs.eles[i] = elr;
            }
        }
        panJs.lel.style.display = "none";
        x.style.display = "";
        panJs.lel = x;
        updMor();
    };
    const tim = () => {
        if (panJs.parb.resiz) {
            panJs.parb.resiz = false;
            overpan();
            setGr();
        }
    };
    const setActivedMy = (name) => {
        let ao, an;
        ao = panJs.nav.querySelector(panJs.act);
        an = panJs.nav.querySelector(name);
        if(ao) ao.setAttribute('data-act', '0');
        if(an) {
            panJs.act = name;
            an.setAttribute('data-act', '1');
            if(an.style.display == "none") replGr(an);
            if(panJs.refes.lin) {
                panJs.refes.lin.style.left = Math.round(an.getBoundingClientRect().left)+"px";
                panJs.refes.lin.style.width = Math.floor(an.getBoundingClientRect().width)+"px";
            }
        }
    };
    const preTim = () => {
        if(!panJs.parb.resiz) {
            panJs.parb.resiz = true;
            panJs.timid = setTimeout(tim,1000);
        }
    };
    const onEdit = (e) => {
        let par = e.target.parentElement;
        par.setAttribute('data-st', '1');
    };
    const onFin = (e) => {
        let par, inp;
        par = e.target.parentElement;
        inp = par.querySelector("input");
        par = par.parentElement;
        if (panJs.inps[inp.id]) {
            if(panJs.edGr){
                if(props.cla) {
                    chGroup(panJs.edGr, inp.value, par);
                } else {
                    dispatch(changeGroups(CHANGE_PANE_GRS, panJs.ke, inp.value, panJs.edGr));
                    par.setAttribute('data-st', '0');
                }
                panJs.edGr = undefined;
                panJs.blockCl = false;
            } else {
                let grop = Object.getOwnPropertyNames(panJs.info.groups);
                if(props.cla) {
                    addGroup(inp.value, par);
                } else {
                    dispatch(changeGroups(CHANGE_PANE_GRS, panJs.ke, inp.value, grop.length == 0 ? 0 : parseInt(grop[grop.length - 1]) + 1));
                    par.setAttribute('data-st', '0');
                }
            }
        }
    };
    const onClose = (e) => {
        let par = e.target.parentElement.parentElement;
        par.setAttribute('data-st', '0');
        panJs.edGr = undefined;
        panJs.blockCl = false;
    };
    const onEdGr = (e) => {
        let par, inp;
        par = e.target.parentElement;
        panJs.blockCl = "ED";
        panJs.panAdd.setAttribute('data-st', 1);
        inp = panJs.panAdd.querySelector("input");
        inp.value = panJs.info.groups[par.getAttribute('data-id')];
        chStatB({target: inp});
        panJs.edGr = par.getAttribute('data-id');
    };
    const onDel = (e) => {
        let par = e.target.parentElement;
        panJs.blockCl = "DEL";
        if(props.cla) {
            remGroup(par.getAttribute('data-id'));
        } else {
            dispatch(changeGroups(CHANGE_PANE_DEL_GRS, panJs.ke, undefined, par.getAttribute('data-id')));
        }
    };
    const chStatB = (e) => {
        let el = e.target;
        panJs.inps[el.id] = !el.validity.patternMismatch && el.value.length != 0;
        if (panJs.inps[el.id]) {
            el.setAttribute("data-mod", '0');
        } else {
            el.setAttribute("data-mod", '1');
        }
        el.parentElement.querySelector(".yes").setAttribute("data-enable", +panJs.inps[el.id]);
    };
    const getAdd = (name, namecl) => {
        if(!props.cla || cState.role != 3) return;
        let cl, cla;
        cl = "pan" + namecl;
        cla = [paneCSS.nav_i, paneCSS.nav_iZag, paneCSS.nav_iJur, cl].join(" ");
        return (
            <div className={cla} data-st="0" ref={(el)=>(panJs.panAdd = el)}>
                <div className={paneCSS.nav_i+" "+paneCSS.chPass} id={paneCSS.nav_i} onClick={onEdit}>
                    {name}
                </div>
                <div className={paneCSS.nav_i+" "+paneCSS.blNew} id={paneCSS.nav_i}>
                    <input className={paneCSS.inp+" "+paneCSS.in} id={"inpt_"} onChange={chStatB} type="text" pattern="^[A-Za-zА-Яа-яЁё\s0-9.-]+$"/>
                    {ele(false, "inpt_", true)}
                    <img className={paneCSS.imginp+" yes "+paneCSS.in} src={yes} onClick={onFin} title="Подтвердить" alt=""/>
                    <img className={paneCSS.imginp+" "+paneCSS.in} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                </div>
            </div>
        )
    };
    const setGroup = (param) => {
        dispatch(changeGroups(gType[props.cla == true][CHANGE_PANE_GR], panJs.ke, param, undefined, panJs.blockCl));
        setGr(true);
    };
    const cState = useSelector(states);
    const paneInfo = useSelector(pane);
    const groupsInfo = useSelector(groups);
    const panJs = useRef(new PanJs()).current;
    const dispatch = useDispatch();
    if(panJs.ke == undefined) {
        panJs.ke = kel++;
        if(!props.cla) {
            dispatch(changeGroups(CHANGE_PANE, panJs.ke, props.gro));
        }
        panJs.info = props.cla ? groupsInfo.els : paneInfo.els[panJs.ke];
    }
    // if(panJs.info) {
    //     panJs.g = Object.getOwnPropertyNames(panJs.info.groups).map(param =>
    //         panJs.info.groups[param] &&
    //         (panJs.gr[param] = getPan(panJs.info.groups[param], param, panJs.info.groups[param].linke, undefined, () => setGroup(param)))
    //     );
    // }
    const isFirstUpdate = useRef(true);
    const [_, forceUpdate] = useReducer((x) => x + 1, 0);
    useEffect(() => {
        dispatch(changeEvents(CHANGE_EVENTS_STEP, 1));
        if((props.cla && cState.role == 3)) {
            panJs.nav.style.gridTemplate = "7vh/ 15% repeat(5,1fr)";
            chStatB({target: panJs.nav.querySelector("." + paneCSS.nav_iZag + " input")});
        }
        console.log("I was triggered during componentDidMount Pane.jsx ke: " + panJs.ke);
        window.addEventListener('resize', preTim);
        panJs.pari.paels = 0;
        overpan();
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_STEP, -1));
            window.removeEventListener('resize', preTim);
            panJs.pari.paels = 0;
            clearTimeout(panJs.timid);
            console.log("I was triggered during componentWillUnmount Pane.jsx ke: " + panJs.ke);
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        if(panJs.parb.updf){
            panJs.parb.updf = false;
            console.log('componentDidUpdate onlyRender Pane.jsx ke: ' + panJs.ke);
            return;
        }
        let el = panJs.nav.querySelectorAll("#pan > .pa");
        if(el.length != panJs.pari.paels) {
            overpan();
            panJs.pari.paels = el.length;
        }
        console.log('componentDidUpdate Pane.jsx ke: ' + panJs.ke);
    });
    return (
        <nav className={paneCSS.panel} id="pan" data-mod={props.main ? "1" : "0"} data-ke={panJs.ke} ref={el=>(panJs.nav = el)}>
            {ele(0, "elems")}
            {getAdd("Добавить группу", "Add")}
            {Object.getOwnPropertyNames(panJs.info.groups).map(param =>
                panJs.info.groups[param] &&
                (panJs.gr[param] = getPan(panJs.info.groups[param], param, panJs.info.groups[param].linke, undefined, () => setGroup(param)))
            )}
            <div className={paneCSS.predBlock} ref={ele=>panJs.mor = ele}>
                {panJs.lmor}
            </div>
            {!props.main &&
                <div className={paneCSS.lin} data-id="1" style={{width: (100 / panJs.pari.elems) + "%"}} ref={ele=>panJs.refes.lin = ele}/>
            }
        </nav>
    )
}
export default Pane;