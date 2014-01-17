using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using Moserware.Skills;
using System.Threading.Tasks;
using Moserware.Skills.TrueSkill;
using System.Net;

namespace ProcessPkrLog
{
	class Program
	{
		static void Main(string[] args)
		{
			Dictionary<string, Player> players = new Dictionary<string, Player>();
			Dictionary<string, int> plays = new Dictionary<string, int>();
			StreamReader reader;
			if (args[0].StartsWith("http://"))
			{
				reader = new StreamReader(new WebClient().OpenRead(args[0]));
			}
			else
			{
				reader = new StreamReader(args[0]);
			}
			StreamWriter writer = new StreamWriter(args[1], false);
			Dictionary<DateTime, Dictionary<string, List<int>>> Games = new Dictionary<DateTime, Dictionary<string, List<int>>>();

			Dictionary<string, List<int>> currentGame = new Dictionary<string, List<int>>();

			string LastLine = "";

			int c = 0;
			int year = 2009;
			int prevousMonth = 0;
			DateTime gamePlayed = DateTime.UtcNow;
			while (!reader.EndOfStream)
			{
				string line = reader.ReadLine().ToLower();
				bool draw = false;
				if (line.StartsWith("="))
				{
					line = line.Substring(1);
					draw = true;
				}

				if (line.Contains("	"))
				{
					line = line.Substring(0, line.IndexOf('	'));
				}

				if (line == "sandy" || line == "bag") { line = "sandbag"; }
				if (line == "will" || line == "will hg") { line = "hg"; }
				if (line == "will g") { line = "billy"; }
				if (line == "mark r") { line = "mark"; }
				if (line == "walking\ndave") { line = "walking dave"; }
				if (line == "bladders") { line = "blads"; }
				if (line == "aussie dan") { line = "dan oz"; }
				if (line == "kitchen" || line == "paul") { line = "paul"; }
				if (line == "jeff" || line == "jefferson") { line = "jeff"; }

				if (!string.IsNullOrEmpty(line))
				{
					if (c > 1)
					{
						if (draw)
						{
							c--;
						}
						int pos = c - 1;

						if (!currentGame.ContainsKey(line))
						{
							currentGame.Add(line, new List<int>());
						}
						currentGame[line].Add(pos);

						if (!players.ContainsKey(line))
						{
							players.Add(line, new Player(line));
							plays.Add(line, 1);
						}
						else
						{
							plays[line]++;
						}
					}
					else if (c == 0)
					{
						int[] dateParts = line.
							Split(' ')[0].
							Split('/').
							Select(datePart => Convert.ToInt32(datePart)).
							ToArray();

						if (dateParts[1] < prevousMonth)
						{
							year++;
						}

						prevousMonth = dateParts[1];

						gamePlayed = new DateTime(year, dateParts[1], dateParts[0]);
					}
				}
				else if (string.IsNullOrEmpty(LastLine))
				{
					c = -1;
					while (Games.ContainsKey(gamePlayed))
					{
						gamePlayed = gamePlayed.AddMilliseconds(1);
					}
					Games.Add(gamePlayed, currentGame);
					currentGame = new Dictionary<string, List<int>>();
				}

				c++;
				LastLine = line;
			}

			if (c > 1)
			{
				Games.Add(gamePlayed, currentGame);
			}

			IDictionary<Player, Rating> scores = players.Values.ToDictionary(pair => pair, pair => GameInfo.DefaultGameInfo.DefaultRating);

			int w = 0;
			Dictionary<DateTime, Dictionary<Player, double>> allScores = new Dictionary<DateTime, Dictionary<Player, double>>();
			foreach (var game in Games)
			{
				w++;
				var oldscores = scores.ToDictionary(score => score.Key, score => score.Value.ConservativeRating);
				IEnumerable<IDictionary<Player, Rating>> sc = game.
					Value.
					SelectMany(pair => 
						new IDictionary<Player, Rating>[] 
						{ 
							ConvertToDic(new KeyValuePair<Player, Rating>(players[pair.Key], scores[players[pair.Key]])) 
						}.
						Union
						(
							pair.
							Value.
							Skip(1).
							Select
							(
								pos => ConvertToDic(new KeyValuePair<Player, Rating>(new Player(pair.Key + "." + Guid.NewGuid().ToString(), players[pair.Key].PartialPlayPercentage, players[pair.Key].PartialUpdatePercentage), scores[players[pair.Key]])))
							)
						).OrderBy(Pair => Pair.First().Key.Id);
				//Where(pair => players.ContainsKey(pair.Key)).
				//Select(pair => convert(players[pair.Key], scores[players[pair.Key]], pair.Value.OrderBy(val => val).Skip(1).Select(id => v.ElementAtOrDefault(Convert.ToInt32(id * scale) - 1)).Select(item => item != null ? item : v.First()))).SelectMany(dict => dict.Select(pair => ConvertToDic(pair)));
				var poses = game.Value.OrderBy(Pair => Pair.Key).
						SelectMany(pair => pair.Value.Select(val => Convert.ToInt32(11 - (10.0 / val)))).
						ToArray();
				var newscores = TrueSkillCalculator.CalculateNewRatings<Player>(GameInfo.DefaultGameInfo, sc, poses);
				newscores = newscores.GroupBy(pair => string.Join("", pair.Key.Id.ToString().TakeWhile(ch => ch != '.'))).ToDictionary(grp => players[grp.Key], grp => new Rating( grp.Average(pair => pair.Value.Mean), grp.Average(pair => pair.Value.StandardDeviation)));
				/*
				 *
				 scores[players[grp.Key]].Mean - grp.Sum(pair => scores[players[grp.Key]].Mean - pair.Value.Mean),
					scores[players[grp.Key]].StandardDeviation - grp.Sum(pair => scores[players[grp.Key]].StandardDeviation - pair.Value.StandardDeviation)
				 */
				foreach (var item in newscores)
				{
					if (players.ContainsKey(item.Key.Id.ToString()))
					{
						scores[item.Key] = item.Value;
					}
				}

				allScores.Add(game.Key, scores.ToDictionary(score => score.Key, score => score.Value.ConservativeRating));
			}

			var avg = scores.Average(name => name.Value.ConservativeRating);
			avg = avg - (avg / 10);
			int gameCount = 150;

			var recentPlayers = Games.
				Values.
				Skip(Games.Count - gameCount).
				SelectMany(game => game.Keys).
				GroupBy(grp => grp).
				Where(grp => grp.Count() > 10).
				Select(grp => grp.Key).
				ToArray();
			string[] playersToCareAbout = plays.
				Where(pair => !recentPlayers.Contains(pair.Key)).
				Select(pair => pair.Key).
				Union(
					scores.
						Where(name=> name.Value.StandardDeviation > 1).
						Select(name => (string)name.Key.Id)
				).
				Distinct().
				ToArray();

			writer.WriteLine("	" + players.Keys.Where(name => !playersToCareAbout.Contains(name)).Aggregate((first, second) => string.IsNullOrEmpty(first) ? second : string.Format("{0}	{1}", first, second)));

			foreach (var Game in allScores.Skip(allScores.Count - gameCount))
			{
				writer.WriteLine(Game.Key.ToString("yyyy/MM/dd") + "	" + players.Values.Where(name => !playersToCareAbout.Contains(name.Id)).Select(name => Game.Value[name].ToString()).Aggregate((first, second) => first == null ? second : string.Format("{0}	{1}", first, second)));
			}

			

			foreach (var score in scores.Where(name => !playersToCareAbout.Contains(name.Key.Id)).OrderBy(pair => pair.Value.ConservativeRating))
			{
				Console.WriteLine("{0}: {1}", score.Key.Id, score.Value.ConservativeRating);
			}
			writer.Close();
			reader.Close();
			Console.ReadKey();
		}

		public static IDictionary<Player, Rating> convert(Player p, Rating r, IEnumerable<Rating> repeats)
		{
			Dictionary<Player, Rating> rat = new Dictionary<Player, Rating>();
			rat.Add(p, r);
			if (repeats.Count() > 1)
			{
				foreach (var item in repeats)
				{
					rat.Add(new Player(p.Id + "." + Guid.NewGuid().ToString()), r);
				}
			}
			return rat;
		}

		public static IDictionary<a, b> ConvertToDic<a, b>(KeyValuePair<a, b> kvp)
		{
			Dictionary<a, b> dict = new Dictionary<a, b>();
			dict.Add(kvp.Key, kvp.Value);
			return dict;
		}
	}
}
