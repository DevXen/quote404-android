#!/usr/bin/env python3
"""Translate bashim chunk_27 batch 10 (input lines 901-1000). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
409977: """С Ответов:

just for fun opened the computer case, but didn't see any windows there. What does it even look like?""",
409978: """<ry> reading the list of kings of Norway: Harald I Fairhair, Erik Bloodaxe, Haakon I the Good...
<ry> of course, after the bloodaxe anyone's good!""",
409979: """xxx: My uncle was born and lives in Belgium. There, apparently, they think of Russia the same as the Americans do.
xxx: He comes to visit us once for New Year and gives us an ushanka hat, valenki and the kids each a matryoshka.
xxx: He, apparently, having walked around Moscow last year and not finding any bears there, realized that Russian traditions are dying out.""",
409980: """Afftobus: you know, I also used to wonder how glamorous chicks with long nails wipe their asses, and why they constantly drag around useless decorative dogs... But then the thought came that it's somehow connected...""",
409981: """Ruslan: Actually bought a Sony hx5 camera, can't figure out what this compass is for there) Didn't find anything really explanatory on the internet.
Greatest: well maybe, so that for example if you're walking with a bunch of (moron) photographers in the forest, and some dude (tits) spots a squirrel, and goes "30 degrees from north" and you all point THERE and photograph the squirrel.""",
409982: """XXX: They paid my salary in products.
YYY: And where do you work?
XXX: At the Goznak factory.""",
409983: """niso: my grandma is killing me. on free evenings she doesn't bake pies, she doesn't clean the house and doesn't dig in the garden, she sits in Photoshop (someone installed it for a laugh for the old lady) and sticks my face onto all kinds of busty chicks!""",
409984: """xxx: why the hell in the morning do I have to bother with mathematical proportions of milk to coffee?
xxx: then sleep with Sofia Kovalevskaya!""",
409985: """Rio: I feel that after the 2012 elections a new holiday will appear in Russia
ххх: and which one?
Rio: Old New President!""",
409986: """BaYAnuga: exactly
BaYAnuga: with each pair of calculus we believe in the stipend less and less""",
409987: """xxx: Russian Post slogan: A letter should be long-awaited""",
409988: """ххх: Why did you stop using condoms?
ууу: The sensations aren't the same! But I manage to pull out in time! Besides, she takes a pregnancy test every week!
ххх: And what will you do with a positive test, when two stripes appear? Run system restore to an earlier state from a backup?""",
409989: """ххх: my comp is so old it can't even handle viruses anymore
ххх: a buddy asked me to get online. well I let him. in the end he caught me a banner.
ххх: and this banner loaded my comp so much that it froze, and Windows offered to close it for me, like a frozen app...""",
409990: """faderer: that's what I don't like about porn - they bring pizza, and nobody eats it... a girl's fridge breaks, and nobody fixes it...""",
409991: """111: I live in Chita. and I'd like to plant a maple on my plot. grown from an acorn.
222: Growing a maple from an acorn is quite difficult - as a rule, an oak grows from an acorn.""",
409992: """Александр Пахомов: on e1 two news together
Александр Пахомов: one: Japan suffered 34 billion dollars in damage
Александр Пахомов: next: Russian officials caused 30 billion dollars damage to the budget
Александр Пахомов: our officials are like a tsunami""",
409993: """она: A friend's wedding is coming soon, I want to catch the bride's bouquet so much!
он: pity they don't throw brains at weddings""",
409994: """Yesterday my wife got lip and eye tattoos.
Since it all hurt quite a bit, she took a painkiller, that is, took "one for the chest".
This morning I had the opportunity to observe Donald Duck, who spent the first half of the night boozing, and the second half - crying.""",
409995: """from Wikipedia:
The film is a prequel to the spin-off of the sequel to the remake of the film made in 1932.""",
409996: """Леночка: oh cool, my best friend gave me shampoo, shower gel, and special aromatic soap for my birthday :happy:
123: stop, don't you sit together at uni?
Леночка: yeah together, so what?
123: seems like your friend is hinting that someone needs to wash )
Леночка is offline now""",
409997: """она: in this hat do I look like a b-girl or a b-boy?
он: you look like a b-gimotic!""",
409998: """evgeny_t: the most precise instrument in the world is not an electron microscope, but my shower - move the lever a millimeter to the side - that's it, burnt balls!""",
409999: """<+Mendor|work> Two in the morning, time to eat.
< Schrodingers_cat> whom?
<+Mendor|work> Meat of young cutlets.""",
410000: """indra: I understood the essence of non-Euclidean geometry not from the linear algebra and analytic geometry course, as I should have, but much later, after my first independent wallpapering.""",
410002: """sick: It's scary to use condoms named "Stork\"""",
410003: """fishmonger: Sardina pilchardus has nothing to do with herring, it's a sardine.
trout: "Sardina pilchardus" is a term from the same series as "perch-greenling" :) Doesn't exist in nature, but exists in stores :)
fishmonger: In this name (perch-greenling) a great meaning was hidden, since for many years they managed to push this fish through under the shelf life for perches. But then some bastard hired an ichthyologist at Rosselkhoz and the happiness ended.""",
410004: """From LJ comments
Сабж - Michelle Obama's dress for 35 bucks.

xxx: Well maybe, of course, there are pricier dresses in the wardrobe, but saying "guys, I'm just like you", I think, is still better than "guys, know your place". Although, everyone has their own opinion here.
yyy: A mantis sneaking up on its victim also kinda says: "I'm just a twig!\"""",
410005: """Czar: hey
Czar: Vova, did I understand you right?
Czar: you're offering me to buy bald tires in Yalta from some shady dude with the nick Bloody_Ass?""",
410006: """A conversation of two girls. One (ХХХ) tells how great it is to work at a big IT company, good team, decent salaries (and so it is).
УУУ: And they don't happen to need economists there?
ХХХ: Well, you know, we do have one girl after the economics faculty, she sits at the reception...""",
410007: """xxx: I'm a star!
yyy: yellow dwarf or blue giant?""",
410008: """xxx: bring me a magnet from Bryansk, or better two.
yyy: fine. I'll bring. two.
xxx: and a fridge. gotta hang them somewhere.""",
410009: """xxx: I think at our work they add sleeping pills even to the coffee...""",
410010: """<Gurman> (moralizing) good cheese is good. bad cheese is bad.
<Hobober> let's develop the thought
<Hobober> cheese is cheese""",
410011: """marmoot: YOU ARE BEAUTIFUL, SHOULD HAVE DONE SUCH A HAIRSTYLE LONG AGO. PARDON THE CAPSLOCK - THERE'S NO CAT LYING ON ME.""",
410012: """Она: I don't eat home chicken
Он: well it's like piranhas don't eat piranhas""",
410013: """Я: It's high time you found a prince
Я: No reason for one horse to walk alone""",
410014: """http://stolica.onego.ru/news/161094.html

Law enforcement officers of Petrozavodsk today conducted an operation to neutralize a suspected explosive device found in a package at the post office: a sex toy was mistaken for a bomb.
Workers of the post office on Dzerzhinsky street heard suspicious rhythmic sounds coming from one of the packages that arrived by mail. Concerned employees mistook them for the ticking of a time bomb and called the militsiya.
Law enforcement officers and a bomb squad arrived at the scene. All visitors and post office staff were evacuated to the street. A specialist in a special protective suit opened the package and found in it a sex toy in the shape of a male genital organ, popularly called a "vibrator". The device was in working mode, which was the cause of the rhythmic sounds inside the package.
Judging by the details on the package, the gift is intended for one of the female residents of Petrozavodsk. After the formalities are completed, she will receive her gift.""",
410015: """lizi: got a job today. The boss looks attentively at the signature and utters: *yes, interesting handwriting, such a signature will be hard to forge, well fine, I'll go show the accountant.*
lizi: should I work there?""",
410016: """XXX: I dreamed I was a sheep in a field, nibbling grass. and a second one comes up to me, winks conspiratorially and says: "I think we're being herded\"...""",
410017: """comments on hentai anime:

ххх: That's why I prefer anime over porn. There's no emotion in porn, just stupid sex. And here you can immediately see - love, feelings...

yyy: Idiot! she's being raped!""",
410018: """If you think that the best toy for your female child would be some big, beautiful doll, you're very wrong! Much more to his taste will be your wireless keyboard, with which he'll hide in some hard-to-reach place and with a face full of bliss will poke at various buttons. while you with foam at the mouth will try to prove to your computer that it's wrong, that you didn't delete a folder with your photos for the last two years a second ago and certainly didn't confirm its deletion.""",
410019: """xxx: Ad: internet club requires an administrator, a girl of attractive appearance, age 19-25, preferably with casino work experience.
xxx: not suspicious at all.""",
410020: """from habr
ххх: ooo! I always solved equations fast in math, and my deskmate — correctly""",
410021: """OktaviA: having played Diablo 2 I realized that the main thing in life is health )""",
410022: """xxx: A total fuckup is when you celebrate your thirtieth birthday at McDonald's...
yyy: .., and shout - "free register!\"""",
410023: """ххх: Why does Dr.Web have a foreign name, it was developed in Russia after all? Could've named it in Russian.
ууу: Viruscatcher of engineer Danilov?""",
410024: """Александр898: What do I need to buy to spread wifi around the apartment?????????
Торнеыч: an apartment""",
410025: """xxx: 7 years ago I did such gymnastics and lost 38 centimeters in a month in total from all body parts, then quit, and now decided to do it again.
yyy: how's that?
yyy: are you a tapeworm?""",
410026: """xxx: I had a colleague with interesting thinking, pity he quit.
xxx: Once I noticed he had his pin code written on his card. Well, naturally, I start explaining that you can't do that etc.
xxx: He looked at me with deep contempt and said that there are deliberately wrong numbers written there.""",
410027: """ххх: listen, if it were urgently needed, would you climb up to my second floor via the drainpipe?
ууу: you know, somewhere in Germany or France I would. But in Russia, where the pipe was fastened by drunk installers some ten years ago with unhardened screws using Chinese tools... hardly.
ххх: you don't love me!
ууу: I just already climbed once!!! (""",
410028: """<Gio> don't speak of the absent, or speak well
<Gio> for example, "good that he's not here\"""",
410029: """Silver Fork: Very often "sorry, I forgot" means "I knew, but thought you wouldn't notice\"""",
410030: """xxx: In the shopping complex near my house there's a toy store "Scare Toys".
xxx: That's exactly what's written there.
xxx: So you can go in and scare the toys. )""",
410031: """xxx: and some pedestrians cross the road so brazenly that it seems like they're saving before the zebra""",
410032: """Конунг: Was throwing out an old toilet with my brother. Came to the dump - it's piled with garbage in a ten-meter radius.
Конунг: Decided to swing it and throw. Swinging...
Конунг: Vovik goes - "Forgive me, Vincenzo, you know too much!"
Конунг: The toilet flew the other way, because my limbs gave out from laughter.""",
410033: """xxx: How lovely these future engineers are.
xxx: I remember how we used to secretly smuggle beer past the guard into the dorm, today I saw them trying to sneakily drag in a moonshine still.""",
410034: """xxx: I've been ignoring a girl for 2 months already. and she keeps writing and calling, writing and calling...
yyy: why are you ignoring her?
xxx: why should I answer her?
yyy: you don't like her?
xxx: I don't like her husband""",
410035: """olhanninen@lj: Do you know what the Marquis de Pombal did after the 1755 Lisbon earthquake with the Jesuit Malagrida, who declared the earthquake God's punishment for the sins of the Lisboners? He initiated a show trial accusing Malagrida of heresy, which consisted in attributing to God a crime - the death of children - and blasphemy, since he claimed to know God's plans. Malagrida was burned. After which the Marquis de Pombal abolished the Inquisition court, and expelled the Jesuit order from Portugal.""",
410036: """xxx: Our calculus prof is a bitter drunk. Two weeks ago he had a birthday. Since then he hasn't appeared at the uni once! Probably still celebrating.
yyy: Yeah... If a drinking mathematician is born 14 days in a row - he can degenerate completely. Into an identity.""",
410037: """xxx: Our local bailiffs, apparently, are the only ones who own the secret of time travel. Only with such a skill can you create a document on February 22, write in it a demand to appear on February 19 and send the document by mail on March 4!""",
410038: """rand0m: never, under no circumstances say the word "processor" in front of users if it's about a laptop
rand0m: you can cause distrust and doubt in users about your qualifications
rand0m: because a PROCESSOR IS A FUCKING BOX, AND A LAPTOP DOESN'T HAVE ONE""",
410039: """Doc_: Was just connecting a network printer to Windows 7, and this buzzer asked me if I trust this printer.
Doc_: No, bitch, I don't trust it!
Doc_: A month ago it borrowed five hundred from me for toner and won't give it back!""",
410040: """ххх: the neighbor kids are getting on my nerves. threw another party in the entrance
ууу: god be with them, don't swear!
ххх: god is not with them! I just looked - it's all minors there!""",
410041: """xxx: bitches
xxx: looked in my work book - 3 years worked as a dark administrator
xxx: that's what's written""",
410042: """xxx: We've come to this.
xxx: First in childhood they forbade playing dendy for bad behavior, then the computer.
xxx: Now mom took my wife away and won't give her back.""",
410043: """ааа: I wonder, have at least half of those who write "The Master and Margarita" in favorite books even read it?
ббб: well... they've heard a lot of good things about it""",
410044: """unconcern: should I worry when the boss's last google query is - "countries where it's warm and there's no extradition\"?""",
410045: """ххх: analyzed the estimate and saved it with the names "estimate" and "estimate proanal." Sent both files to the management. A minute later a call: "Which estimate should I look at, the regular one or the one about sex?\"""",
410046: """xxx: I'm hard to offend!!
yyy: I know
yyy: You're hard to find, easy to lose and impossible to forget
xxx: how???
xxx: how do you know everything????""",
410047: """Chamie: Couldn't get through this test. With these answer options I can't pass it.
Chamie: If there were a question "What's your favorite tea?", the answers would be "1. Sweet. 2. Hot. 3. Black. 4. Liquid\"""",
410048: """xxx: The first computer war started after a penguin smashed a window with an apple.""",
410049: """AppleTree: trollfat is what's left after trolling in chats""",
410050: """<NitrouZ> There's a car standing under the windows, with "ANYA, I LOVE YOU" written on it in paint
<NitrouZ> Knowing that Anya is underage, and this is her father's car, she clearly won't get the blessing..)""",
410051: """Mom talking to grandma on the phone:
- Tomorrow, when you show the dacha to potential buyers, don't forget to first take the coffins out of the house and hide them somewhere. Or we'll never sell this dacha.""",
410052: """xxx: Yeeah, spring. Sailed to the store for some bread.""",
410053: """<Val> This moron got put in the hospital, he was faking a concussion... when two little nurses got pregnant by him, the brain-disabled, like a real man, left through the window.""",
410054: """xxx: Yesterday watched a cute little film about the life of ants
xxx: A handsome prince flies to his princess, first thing the worker ants gnaw off his wings, and drag him already wingless
yyy: they're right to do so )
zzz: drag where?
yyy: obviously where )
yyy: to the registry office!""",
410055: """xxx:
There was a funny thing. The boss gave the admin a task, writing it on a piece of paper. Today the admin returned the paper along with a memo: didn't complete the task, because the whole department couldn't decipher the handwriting. Next time write more carefully""",
410056: """February. Frost -17. Formation on the parade ground.
Commander:
- today, the fifth day of exercises, operational time: July 18, 2011...
An indifferent voice from deep in the formation:
- What a shitty summer...""",
410057: """alxspb: and my performance index on the laptop is FOUR AND A HALF! AND YOURS?!
Feelixez: genion taz # cat /proc/cpuinfo | grep bogo
bogomips : 3199.99
Feelixez: better show me this, it tells me more
alxspb: so a chick comes up to you and asks, like so how's your laptop? And you give her these mipses. She thinks and thinks, and walks on
Feelixez: HAHA
Feelixez: THE CHICK WILL LOOK OVER MY SHOULDER AND SAY OOOO LOR OOOOO YOU HAVE FIVE STARS AND YOU'RE A MODERATOR TOO?!??!?!
alxspb: and will tenderly tickle you with her beard""",
410058: """xxx: the primordial Russian laziness shows in very cute nuances.
xxx: in the entrance they painted the windowsill along with the ashtray and the cigarette butts.""",
410059: """Оркус: damn, how do I explain to her that four socks and two pairs of socks are not the same thing?""",
410060: """Аманда: I really like him. I think I'll kill any woman who encroaches on our relationship. and that's not a joke )
Jen: honey, how you've grown up ) praiseworthy, praiseworthy
Аманда: you're my most adequate close person. All the sweetie-girlfriends I told about the murder unanimously wailed "What, what, we're friends!" Haha, yeah yeah... )""",
410061: """xxx: Looking for a supply manager. Yesterday they bring a resume: studied at a decent uni, red diploma, worked in this specialty, etc. Contacts - home phone, cell phone, e-mail - MEGATRAKHER88.
xxx: Epic fail.""",
410062: """Он: Hi, my precious :)
Она: Hi, gollumushka )))""",
410063: """kitya@lj: That day I came to work in trousers and a shirt (instead of the usual jeans and t-shirt, like everyone wears here). Colleagues got scared, started running around. The boss came up, asked, as if joking, whether I was going to an interview. I, of course, reassured him that I wasn't going to quit yet.
And the very next day I was promoted.
Simply the clean t-shirts at home ran out, what an effect.""",
410064: """xxx: The road to hell is paved with good intentions""",
410065: """A friend divorced her husband, no sex for 4 months already, and yesterday put a condom in her wallet. To my question "Why?", she answered: "Luring. You know, there's such a superstition - always carry a ruble in your wallet so money comes\".""",
410066: """xxx: by the way recently took apart one China-phone - the "gyroscope" there is imitated quite simply.
there's a button like on a limit switch and a lever fixed on a hinge. when a person turns the phone - the lever falls and the picture on the screen turns. such is the analog sensor. strange that the battery isn't fueled by lighter gas""",
410067: """он: don't you want to go somewhere and grab a bite?
она: bite through the phone cable. and don't call. you're pissing me off.""",
410068: """ОНА: and we didn't use protection
ОНА: )))
ОН: we did use protection
ОН: I prayed""",
410069: """ххх: and don't bat your eyelashes at me, won't help
ууу: fine. I'll look serious. Like at an integral. With the look of "about to take it\".""",
410070: """xxx: There are numbers 1 3 4 and 6. Need to get 24 using addition, subtraction and multiplication. Use each digit only once.
yyy: Listen, send it to me when I'm at work - I've got things to do at home.""",
410071: """I work as an admin and information security engineer at a polyclinic. Right now changing the coloring film in the fax in accounting, looking at it against the light and seeing letters of printed documents. Showing it to the (C)hief accountant.
Я: As an information security engineer I must ...
Г: ... eat it?""",
410072: """Changed a friend's brake discs on his car. The garage consists of many boxes. An Audi A8 arrives, drives into its box. A man of 30-35 gets out of the car, puts the car on the alarm, leaving the box presses a button, the automatic gates close, the light in the box goes out. The man unhurriedly leaves the garage. The next 15-20 minutes we work. Then the Audi owner comes running back, on the run opens the gates, takes the car off the alarm, opens the car, takes his DAUGHTER out of the car, closes the car, the box gates, leaves the garage holding his daughter by the hand.

And the girl is calm, apparently not the first time.""",
410073: """xxx: what was on classes today?
yyy: for example, there was talk about professions. Dasha said that now it's prestigious and popular to be an engineer. I said that now it's popular to be an admin's cat, and being an engineer is just interesting.""",
410074: """она: - right now you'll linger, be late for work and explain: "couldn't get out from under my girlfriend"
он: - yeah, and then I'll linger after work and explain: "couldn't get out from under my boss\"...""",
410075: """ChaynikUA: spring has come - the speed from the porn tracker dropped again""",
410076: """From the discussion "how to suppress sexual desire":

xxx: I'm constantly tormented by sexual desire, it interferes with my work, doesn't let me focus on work. There's no beloved sexual partner, and I can't do it with just anyone, since feelings are the main thing for me. What to do, how to overcome, muffle it?

yyy: Celentano's hero chopped wood...""",
410077: """ххх:
he already called me hysterical once, when I cried during a movie and wiped my snot with his cat""",
}

if __name__ == "__main__":
    import sys
    src = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_27.jsonl"
    dst = "/home/hatch/workspace/translation-work/bashim/out/chunk_27.jsonl"
    start, end = 901, 1000  # 1-based line numbers
    lines = open(src, encoding="utf-8").read().splitlines()
    assert len(lines) >= end, f"only {len(lines)} lines"
    # Build and validate everything BEFORE appending (avoids partial writes).
    out = []
    want_ids = []
    for ln in range(start, end + 1):
        r = json.loads(lines[ln - 1])
        en = EN.get(r["id"])
        assert en is not None, f"missing translation for id {r['id']} (line {ln})"
        assert en, f"empty en for id {r['id']}"
        assert en.count("\n") == r["text"].count("\n"), (
            f"newline mismatch id {r['id']}: en={en.count(chr(10))} ru={r['text'].count(chr(10))}")
        out.append(json.dumps({"id": r["id"], "ru": r["text"], "en": en}, ensure_ascii=False))
        want_ids.append(r["id"])
    # Resume safety: skip ids already present in dst.
    have = set()
    try:
        with open(dst, encoding="utf-8") as f:
            for l in f:
                l = l.strip()
                if l:
                    have.add(json.loads(l)["id"])
    except FileNotFoundError:
        pass
    new = [o for o, i in zip(out, want_ids) if i not in have]
    with open(dst, "a", encoding="utf-8") as f:
        for o in new:
            f.write(o + "\n")
    print(f"batch lines {start}-{end}: {len(new)} appended "
          f"({len(out) - len(new)} already present)")
